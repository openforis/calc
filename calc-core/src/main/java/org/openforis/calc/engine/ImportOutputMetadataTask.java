/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.engine.WorkspaceBackup.Phase1Data;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryHierarchy;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.CategoryLevel.CategoryLevelValue;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.EquationManager;
import org.openforis.calc.metadata.MetadataManager;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesignManager;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.CalcSchema;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.persistence.jooq.tables.daos.StratumDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.TableDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class ImportOutputMetadataTask extends Task{
	@JsonIgnore
	private WorkspaceBackup workspaceBackup;
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;	
	@Autowired
	private StratumDao stratumDao;
	@Autowired
	private AoiManager aoiManager;
	@Autowired
	private CategoryManager categoryManager;
	@Autowired
	private CalculationStepDao calculationStepDao; 
	@Autowired
	private EquationManager equationManager;
	@Autowired
	private Psql psql;
	@Autowired
	private TableDao tableDao;
	@Autowired
	private MetadataManager metadataManager;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private EntityDao entityDao;
	@Autowired
	private WorkspaceDao workspaceDao;
	
	@Override
	protected long countTotalItems() {
		return 9;
	}
	
	@Override
	protected void execute() throws Throwable {
		
		importPhase1Data();
		importStrata();
		importPlotAreaScript();
		importOutputCategories();
		importExternalEquations();
		importSamplingDesign();
		importAois(); 
		importOutputVariables();
		importCalculationSteps();
		
	}
	
	private void importCalculationSteps() {
		
		incrementItemsProcessed();
	}

	private void importAois() {
		List<AoiHierarchy> aoiHierarchies = getWorkspaceToImport().getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			aoiManager.createFromBackup( getWorkspace() , aoiHierarchy );
		}
		
		incrementItemsProcessed();
	}

	private void importSamplingDesign() {
		if( getWorkspaceToImport().hasSamplingDesign() ){
			SamplingDesign samplingDesign = getWorkspaceToImport().getSamplingDesign();
			
			Entity entity = getWorkspaceToImport().getEntityById( samplingDesign.getSamplingUnitId() );
			Entity samplingUnit = getWorkspace().getEntityByOriginalId( entity.getOriginalId() );
			
			samplingDesign.setSamplingUnit(samplingUnit);
			
			samplingDesignManager.insert( getWorkspace(), samplingDesign  );
		}
		
		incrementItemsProcessed();
	}

	private void importExternalEquations() {
		List<EquationList> equationLists = getWorkspaceToImport().getEquationLists();
		for ( EquationList equationList : equationLists ) {
			
			Long equationListId = equationList.getId();
			equationManager.create( getWorkspace() , equationList );
			Long newEquationListId = equationList.getId();
			
			List<CalculationStep> calculationSteps = getWorkspaceToImport().getDefaultProcessingChain().getCalculationSteps();
			for ( CalculationStep calculationStep : calculationSteps ) {
				if( calculationStep.getType() == Type.EQUATION && equationListId.equals(calculationStep.getEquationListId()) ){
					calculationStep.setEquationListId( newEquationListId );
				}
			}
			
		}
		
		incrementItemsProcessed();
	}

	private void importOutputCategories() {
		List<Category> categories = getWorkspaceToImport().getCategories();
		for ( Category category : categories ) {
			if( category.isUserDefined() ){
				
				CategoryHierarchy hierarchy = category.getHierarchies().get(0);
				CategoryLevel level = hierarchy.getLevels().get(0);
				
				Integer oldCategoryId = category.getId();
				Integer oldCategoryLevelId = level.getId();
				
				getWorkspace().addCategory( category );
				List<CategoryLevelValue> values = workspaceBackup.getCategoryLevelValues().get( level.getId() );
				
				categoryManager.createCategory( getWorkspace(), category , values );
				
				Integer newCategoryId = category.getId();
				// replace categoryId in calc step of type category  
				List<CalculationStep> calculationSteps = getWorkspaceToImport().getDefaultProcessingChain().getCalculationSteps();
				for ( CalculationStep calculationStep : calculationSteps ) {
					if( calculationStep.getType() == Type.CATEGORY ){
						Integer stepCategoryId = calculationStep.getParameters().getInteger( "categoryId" );
						if( stepCategoryId.equals(oldCategoryId) ){
							calculationStep.getParameters().setInteger( "categoryId" , newCategoryId );
						}
					}
				}

				level = hierarchy.getLevels().get(0);
				Integer newCategoryLevelId = level.getId();
				// replace categoryId in user defined variables				
				Collection<Variable<?>> userDefinedVariables = getWorkspaceToImport().getUserDefinedVariables();
				for ( Variable<?> variable : userDefinedVariables ) {
					if( variable.getCategoryLevelId() != null && oldCategoryLevelId.equals(variable.getCategoryLevelId().intValue()) ) {
						variable.setCategoryLevelId(newCategoryLevelId.longValue());
					}
				}
				
			}
		}
		
		incrementItemsProcessed();
	}

	private void importStrata() {
		List<Stratum> strata = getWorkspaceToImport().getStrata();
		getWorkspace().setStrata(strata);
		stratumDao.insert( getWorkspace().getStrata() );
		
		incrementItemsProcessed();
	}

	private void importPlotAreaScript() {
		List<Entity> entities = getWorkspaceToImport().getEntities();
		for ( Entity entity : entities ) {
			String plotAreaScript = entity.getPlotAreaScript();
			if( StringUtils.isNotBlank(plotAreaScript) ){
				Entity originalEntity = getWorkspace().getEntityByOriginalId( entity.getOriginalId() );
				if( originalEntity == null ){
					throw new IllegalStateException( "Entity " + entity.getName() + " not found in workspace" );
				}
				originalEntity.setPlotAreaScript(plotAreaScript);
				entityDao.update( originalEntity );
			}
		}
		
		incrementItemsProcessed();
	}

	private void importOutputVariables() {
		Collection<Variable<?>> userDefinedVariables = getWorkspaceToImport().getUserDefinedVariables();
		for ( Variable<?> variable : userDefinedVariables ) {
			
			Entity originalEntity = getWorkspaceToImport().getEntityById( variable.getEntityId() );
			Entity entity = getWorkspace().getEntityByOriginalId( originalEntity.getOriginalId() );
			if( entity == null ){
				throw new IllegalStateException( "Entity " + variable.getEntity().getName() + " not found in workspace" );
			}
			entity.addVariable(variable);
			
			// set new id to variable 
			Integer variableId = variable.getId();
			int newVariableId = psql.nextval( Sequences.VARIABLE_ID_SEQ ).intValue();
			variable.setId( newVariableId );
			Scale scale = (variable instanceof QuantitativeVariable ) ? Scale.RATIO : Scale.NOMINAL;
			variable.setScale( scale  );
			variableDao.insert( variable );
			
			// replace output variable for calculation steps
			List<CalculationStep> calculationSteps = getWorkspaceToImport().getDefaultProcessingChain().getCalculationSteps();
			for ( CalculationStep calculationStep : calculationSteps ) {
				if( calculationStep.getOutputVariableId().equals(variableId) ){
					calculationStep.setOutputVariable(variable);
				}
			}
		}
	}

	private <T extends Object> void importPhase1Data() {
		Phase1Data phase1Data = this.workspaceBackup.getPhase1Data();
		if( phase1Data != null ){
			DynamicTable<?> table = new DynamicTable<Record>( getWorkspaceToImport().getPhase1PlotTableName(), CalcSchema.CALC.getName() );
			table.initFields( phase1Data.getTableInfo() );
			@SuppressWarnings( "unchecked" )
			Field<T>[] tableFields = (Field<T>[]) table.fields();
			
			// create table
			psql
				.createTable( table, tableFields )
				.execute();
			psql
				.alterTable( table )
				.addPrimaryKey( table.getPrimaryKey() )
				.execute();
			// populate table
			List<Query> queries = new ArrayList<Query>();
			for ( DataRecord dataRecord : phase1Data.getRecords() ) {
				InsertQuery<?> insert = psql.insertQuery( table );
				for ( Field<T> field : tableFields ) {
					@SuppressWarnings( "unchecked" )
					T value = (T) dataRecord.getValue( field.getName() );
					insert.addValue( field, value );
				}
				queries.add( insert );
				
				if( queries.size() % 5000 == 0 ){
					psql
						.batch(queries)
						.execute();
					queries.clear();
				}
			}
			psql
				.batch(queries)
				.execute();
			
			Workspace workspace = getWorkspace();
			workspace.setPhase1PlotTable( table.getName() );
			workspaceDao.update( workspace );
		}
		incrementItemsProcessed();
	}

	void setWorkspaceBackup( WorkspaceBackup workspaceBackup ) {
		this.workspaceBackup = workspaceBackup;
	}
	
	@JsonIgnore
	public Workspace getWorkspaceToImport() {
		return this.workspaceBackup.getWorkspace();
	}
	
	private Variable<?> findVariable( Integer variableId ) {
		Map<Integer, Integer> inputVariables = this.workspaceBackup.getInputVariables();
		Integer originalId = inputVariables.get(variableId);
		Variable<?> variable = getWorkspace().getVariableByOriginalId( originalId );
		return variable;
	}
	
//	private Entity findEntity( Integer entityId ) {
//		Map<Integer, Integer> inputEntities = this.workspaceBackup.getInputEntities();
//		Integer originalId = inputEntities.get(entityId);
//		Entity entity = getWorkspace().getEntityByOriginalId( originalId );
//		return entity;
//	}

}
