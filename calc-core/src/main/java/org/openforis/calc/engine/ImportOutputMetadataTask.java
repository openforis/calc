/**
 * 
 */
package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.engine.WorkspaceBackup.Phase1Data;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EquationManager;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesignManager;
import org.openforis.calc.metadata.Stratum;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.persistence.jooq.CalcSchema;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.daos.EntityDao;
import org.openforis.calc.persistence.jooq.tables.daos.StratumDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
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
	private EquationManager equationManager;
	@Autowired
	private Psql psql;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private EntityDao entityDao;
	@Autowired
	private WorkspaceDao workspaceDao;
	@Autowired
	private ProcessingChainManager processingChainManager;
	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	protected long countTotalItems() {
		return 9;
	}
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		
		importPhase1Data( workspace , workspaceBackup );
		importStrata( workspace , workspaceBackup );
		importPlotAreaScript( workspace , workspaceBackup );

		categoryManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		equationManager.importBackup( workspace , workspaceBackup );
		incrementItemsProcessed();
		
		samplingDesignManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		aoiManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		importVariables( workspace , workspaceBackup );
		
		processingChainManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		// reload workspace
		workspace = workspaceService.fetchByCollectSurveyUri( workspace.getCollectSurveyUri() );
		workspaceService.resetResults(workspace);
		getJob().setWorkspace( workspace );
		getJob().setSchemas( workspace.schemas() );
	}

	private void importStrata( Workspace workspace, WorkspaceBackup workspaceBackup ) {
		List<Stratum> strata = workspaceBackup.getWorkspace().getStrata();
		
		workspace.setStrata( strata );
		stratumDao.insert( workspace.getStrata() );
		
		incrementItemsProcessed();
	}

	private void importPlotAreaScript( Workspace workspace, WorkspaceBackup workspaceBackup ) {
		List<Entity> entities = workspaceBackup.getWorkspace().getEntities();
		for ( Entity entity : entities ) {
			
			String plotAreaScript = entity.getPlotAreaScript();
			
			if( StringUtils.isNotBlank(plotAreaScript) ){
				Entity originalEntity = workspace.getEntityByOriginalId( entity.getOriginalId() );
				if( originalEntity == null ){
					throw new IllegalStateException( "Entity " + entity.getName() + " not found in workspace" );
				}
				originalEntity.setPlotAreaScript(plotAreaScript);
				entityDao.update( originalEntity );
			}
		}
		
		incrementItemsProcessed();
	}

	private void importVariables( Workspace workspace, WorkspaceBackup workspaceBackup ) {
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
		
		List<CalculationStep> calculationSteps = workspaceToImport.getDefaultProcessingChain().getCalculationSteps();
		
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
		
		// replace variable references for calculation steps
		for ( CalculationStep calculationStep : calculationSteps ) {
			
			ParameterMap parameters = calculationStep.getParameters();
			
			switch ( calculationStep.getType() ) {
			case EQUATION:
				
				Integer codeVariableId = parameters.getInteger( "code_variable" );
				Integer newCodeVariableId = variableIds.get( codeVariableId );
				parameters.setInteger( "code_variable" , newCodeVariableId );
				
				List<ParameterMap> list = parameters.getList( "variables" );
				for ( ParameterMap param : list ) {
					Integer varId = param.getInteger( "variableId" );
					Integer newVarId = variableIds.get( varId );
					param.setInteger( "variableId" , newVarId );
				}
				
			case SCRIPT:
			case CATEGORY:
				Integer oldVariableId =  calculationStep.getOutputVariableId() ;
				Integer variableId = variableIds.get( oldVariableId );
				Variable<?> outputVariable = workspace.getVariableById( variableId );
				calculationStep.setOutputVariable( outputVariable );
				break;
			default:
				break;
			}
		}
		
		incrementItemsProcessed();
	}

	private <T extends Object> void importPhase1Data(Workspace workspace, WorkspaceBackup workspaceBackup ) {
		Phase1Data phase1Data = workspaceBackup.getPhase1Data();
		Workspace workspaceToImport = workspaceBackup.getWorkspace();
		
		if( phase1Data != null ){
			DynamicTable<?> table = new DynamicTable<Record>( workspaceToImport.getPhase1PlotTableName(), CalcSchema.CALC.getName() );
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
			
			
			workspace.setPhase1PlotTable( table.getName() );
			workspaceDao.update( workspace );
		}
		
		incrementItemsProcessed();
	}

	void setWorkspaceBackup( WorkspaceBackup workspaceBackup ) {
		this.workspaceBackup = workspaceBackup;
	}
	
}
