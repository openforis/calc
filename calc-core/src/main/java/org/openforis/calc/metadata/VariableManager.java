/**
 * 
 */
package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.SelectLimitStep;
import org.json.simple.JSONObject;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepCategoryClassParameters;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.metadata.CategoryLevel.CategoryLevelValue;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.daos.VariableDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * @author S. Ricci
 *
 */
@Repository
public class VariableManager {
	
	@Autowired
	private VariableDao variableDao;
	
	@Autowired
	private CategoryManager categoryManager;
	
	@Autowired
	private Psql psql;

	public long countCategoryClasses(CategoryDimensionTable table ) {
		
		Long count = psql
			.selectCount()
			.from(table)
			.fetchOne( 0 , Long.class );
		
		return count;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> getCategoryClasses(CategoryDimensionTable table ){
		
		List<JSONObject> categories = new ArrayList<JSONObject>(); 
		
		SelectLimitStep<Record2<String, String>> select = psql
			.select( table.getCodeField() , table.getCaptionField() )
			.from( table )
			.orderBy( table.getIdField() );
		 
		Result<Record2<String, String>> result = select.fetch();
		
		for (Record2<String, String> record : result) {
			
			JSONObject json = new JSONObject();
			json.put( "code" , record.value1() );
			json.put( "caption" , record.value2() );
			
			categories.add( json );
		}
		
		return categories;
	}
	
	@Transactional
	public void loadByWorkspace( Workspace workspace ) {
		List<Entity> entities = workspace.getEntities();

		Integer[] entityIds = new Integer[entities.size()];
		for ( int i = 0; i < entities.size(); i++ ) {
			entityIds[i] = entities.get(i).getId();
		}

		psql
			.select()
			.from( Tables.VARIABLE )
			.where( Tables.VARIABLE.ENTITY_ID.in(entityIds) )
			.fetch( new VariableRecordMapper(workspace) );
	}
	
	@Transactional
	public void save( Variable<?>... variables ) {
		if( variables != null ) {
			
			for (int i = 0; i < variables.length; i++) {
				Variable<?> variable = variables[i];
				// set entityId
				Entity entity = variable.getEntity();
				if( entity == null ){
					throw new IllegalStateException( "Found detached variable" );
				}
				variable.setEntityId( entity.getId() );
				// set sortOrder
				variable.setSortOrder( i + 1 );
				if( variable instanceof CategoricalVariable<?> && ((CategoricalVariable<?>) variable).getCategoryLevel() != null ){
					CategoryLevel categoryLevel = ( (CategoricalVariable<?>) variable ).getCategoryLevel();
					variable.setCategoryLevelId( categoryLevel.getId().longValue() );
				}
				
				// insert or update variable
				if( variable.getId() == null ) {
					
					Long nextval = psql.nextval( Sequences.VARIABLE_ID_SEQ );
					int varId = nextval.intValue();
					variable.setId( varId );
					
					variableDao.insert( variable );
				} else {
					variableDao.update( variable );
				}
			}
		
		}

	}

	@Transactional
	public void delete( Variable<?> variable ) {
		Entity entity = variable.getEntity();
		entity.removeVariable( variable );
		variableDao.delete( variable );
	}
	
	@Transactional
	public void deleteUserDefinedVariables( Workspace workspace ){
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			Collection<Variable<?>> userDefinedVariables = entity.getUserDefinedVariables();
			variableDao.delete( userDefinedVariables.toArray( new Variable<?>[]{} ) );
			entity.deleteOutputVariables();
		}
	}
	
	@Transactional
	public void importBackup( Workspace workspace , WorkspaceBackup workspaceBackup ) {

		// first prepare a map with old ids -> new ids
		Map<Integer, Integer> variableIds = new HashMap<Integer, Integer>();
		
		Map<Integer, Integer> originalIds = workspaceBackup.getVariableOriginalIds();
		for ( Integer oldCalcId : originalIds.keySet() ) {
			Integer originalId = originalIds.get(oldCalcId);
			if( originalId != null ){
				Variable<?> variable = workspace.getVariableByOriginalId( originalId  );
				variableIds.put( oldCalcId, variable.getId() );
			}
		}
		
		Workspace workspaceToImport = workspaceBackup.getWorkspace();
		
		// import new variables
		Collection<Variable<?>> userDefinedVariables = workspaceToImport.getUserDefinedVariables();
		for ( Variable<?> variable : userDefinedVariables ) {
			
			Entity originalEntity = workspaceToImport.getEntityById( variable.getEntityId() );
			Entity entity = workspace.getEntityByOriginalId( originalEntity.getOriginalId() );
			if( entity == null ){
				throw new IllegalStateException( "Entity " + variable.getEntity().getName() + " not found in workspace" );
			}
			entity.addVariable( variable );
			
			Integer variableId = variable.getId();
			// set new id to variable 
			variable.setId( psql.nextval( Sequences.VARIABLE_ID_SEQ ).intValue() );
			Scale scale = (variable instanceof QuantitativeVariable ) ? Scale.RATIO : Scale.NOMINAL;
			variable.setScale( scale  );
			variableDao.insert( variable );
			
			variableIds.put( variableId , variable.getId() );
		}
		
		// replace variable references of calculation steps
		List<CalculationStep> calculationSteps = workspaceToImport.getDefaultProcessingChain().getCalculationSteps();
		for ( CalculationStep calculationStep : calculationSteps ) {
			
			ParameterMap parameters = calculationStep.getParameters();
			
			switch ( calculationStep.getType() ) {
			case EQUATION:
				
				Integer codeVariableId = parameters.getInteger( "codeVariable" );
				Integer newCodeVariableId = variableIds.get( codeVariableId );
				parameters.setInteger( "codeVariable" , newCodeVariableId );
				
				List<ParameterMap> list = parameters.getList( "variables" );
				for ( ParameterMap param : list ) {
					Integer varId = param.getInteger( "variableId" );
					Integer newVarId = variableIds.get( varId );
					param.setInteger( "variableId" , newVarId );
				}
				
				replaceOutputVariableId(workspace, variableIds, calculationStep);
				
				break;
			case SCRIPT:
				replaceOutputVariableId(workspace, variableIds, calculationStep);
				break;
			case CATEGORY:
				
				replaceOutputVariableId(workspace, variableIds, calculationStep);
				
				Integer categoryId = calculationStep.getParameters().getInteger( "categoryId" );
				Category category = workspace.getCategoryById( categoryId );
				CategoryLevel level = category.getDefaultLevel();

				// replace category class ids in calculation steps
				List<CalculationStepCategoryClassParameters> categoryClassParameters = calculationStep.getCategoryClassParameters();
				for ( CalculationStepCategoryClassParameters classParameters : categoryClassParameters ) {
					String classCode = classParameters.getClassCode();
					
					CategoryLevelValue levelValue = categoryManager.loadCategoryLevelValue( level, classCode );
					
					classParameters.setClassId( levelValue.getId().intValue() );
					
					Integer variableId = variableIds.get( classParameters.getVariableId() );
					classParameters.setVariableId( variableId );
				}
				break;
			default:
				break;
			}
		}
		
		// replace variable ids in error settings
		ErrorSettings errorSettings 	= workspaceToImport.getErrorSettings();
		ErrorSettings newErrorSettings 	= new ErrorSettings();
		if( errorSettings != null ){
			for ( String key : errorSettings.getParameters().keys() ) {
				long variableId 	= Long.parseLong(key);
				long newVariableId	= variableIds.get( (int)variableId );
				
				Collection<? extends Number> categoricalVariableIds = errorSettings.getCategoricalVariables(variableId);
				List<Long> newCategoricalVariableIds = new ArrayList<Long>();
				for ( Number oldCategoryId : categoricalVariableIds ){
					long newCategoryId = variableIds.get( oldCategoryId.intValue() ).longValue();
					newCategoricalVariableIds.add( newCategoryId );
				}
				errorSettings.setCategoricalVariables(variableId, newCategoricalVariableIds);
			
				newErrorSettings.addErrorSettings( newVariableId, errorSettings.getAois(variableId), newCategoricalVariableIds );
			}
		}
		workspace.setErrorSettings(newErrorSettings);
		workspaceToImport.setErrorSettings(newErrorSettings);
	}

	private void replaceOutputVariableId( Workspace workspace , Map<Integer, Integer> variableIds , CalculationStep calculationStep ) {
		Integer oldVariableId =  calculationStep.getOutputVariableId() ;
		Integer variableId = variableIds.get( oldVariableId );
		Variable<?> outputVariable = workspace.getVariableById( variableId );
		calculationStep.setOutputVariable( outputVariable );
	}
	
}
