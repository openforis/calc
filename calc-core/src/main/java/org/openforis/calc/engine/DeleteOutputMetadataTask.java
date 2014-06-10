package org.openforis.calc.engine;

import org.jooq.Record;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.EquationManager;
import org.openforis.calc.metadata.MetadataManager;
import org.openforis.calc.persistence.jooq.CalcSchema;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.EntityTable;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.SamplingDesignDao;
import org.openforis.calc.persistence.jooq.tables.daos.StratumDao;
import org.openforis.calc.persistence.jooq.tables.daos.WorkspaceDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.TableDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Mino Togna
 *
 */
public class DeleteOutputMetadataTask extends Task {
	
	@Autowired
	private SamplingDesignDao samplingDesignDao;	
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
	private WorkspaceDao workspaceDao;
	
	public DeleteOutputMetadataTask() {
	}
	
	@Override
	protected long countTotalItems() {
		return 8;
	}
	
	@Override
	protected void execute() throws Throwable {
		deleteCalculationSteps();
		deletePhase1Data();
		deleteExternalEquations();
		deleteSamplingDesign();
		deleteOutputCategories();
		deleteAois(); 
		deletePlotAreaScript();
		deleteOutputVariables();
	}

	private void deletePhase1Data() {
		Workspace workspace = getWorkspace();
		DynamicTable<?> phase1Table = new DynamicTable<Record>( workspace.getPhase1PlotTableName(), CalcSchema.CALC.getName() );
		if( tableDao.exists( phase1Table ) ){
			psql
				.dropTableIfExists(phase1Table)
				.execute();
		}
		
		workspace.setPhase1PlotTable( null );
		workspaceDao.update( workspace );
		
		incrementItemsProcessed();
	}

	private void deleteOutputVariables() {
		metadataManager.deleteUserDefinedVariables(getWorkspace());
		
		incrementItemsProcessed();
	}

	private void deleteCalculationSteps() {
		ProcessingChain processingChain = getWorkspace().getDefaultProcessingChain();
		calculationStepDao.delete( processingChain.getCalculationSteps() );
		processingChain.clearCalculationSteps();
		
		incrementItemsProcessed();
	}

	private void deletePlotAreaScript() {
		EntityTable T = Tables.ENTITY;
		psql
			.update( T )
			.set( T.PLOT_AREA_SCRIPT, (String) null )
			.where( T.WORKSPACE_ID.eq(getWorkspace().getId()) )
			.execute();
		
		incrementItemsProcessed();
	}

	private void deleteAois() {
		// delete aois
		aoiManager.delete( getWorkspace() );
		
		incrementItemsProcessed();
	}

	private void deleteOutputCategories() {
		// delete output categories
		categoryManager.deleteOutputCategories( getWorkspace() );
		
		incrementItemsProcessed();
	}

	private void deleteSamplingDesign() {
		// delete sampling design
		if( getWorkspace().hasSamplingDesign() ){
			samplingDesignDao.delete( getWorkspace().getSamplingDesign() );
			getWorkspace().setSamplingDesign( null );
		}
		stratumDao.delete( getWorkspace().getStrata() );
		getWorkspace().emptyStrata();
		
		incrementItemsProcessed();
	}

	private void deleteExternalEquations() {
		equationManager.delete( getWorkspace() );
		
		incrementItemsProcessed();
	}
	
	
	@Override
	public String getName() {
		return "Delete "  + getWorkspace().getName() + " calc metadata";
	}
	
}
