package org.openforis.calc.engine;

import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.metadata.AoiManager;
import org.openforis.calc.metadata.CategoryManager;
import org.openforis.calc.metadata.EntityManager;
import org.openforis.calc.metadata.EquationManager;
import org.openforis.calc.metadata.ErrorSettingsManager;
import org.openforis.calc.metadata.SamplingDesignManager;
import org.openforis.calc.metadata.VariableManager;
import org.openforis.calc.metadata.WorkspaceSettingsManager;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Task responsible for deleting all calc metadata associated with the job workspace
 * 
 * @author Mino Togna
 *
 */
public class DeleteOutputMetadataTask extends Task {
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;
	@Autowired
	private AoiManager aoiManager;
	@Autowired
	private CategoryManager categoryManager;
	@Autowired
	private EquationManager equationManager;
	@Autowired
	private ProcessingChainManager processingChainManager;
	@Autowired
	private VariableManager variableManager;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private ErrorSettingsManager errorSettingsManager;
	@Autowired
	private Psql psql;
	@Autowired
	private WorkspaceSettingsManager workspaceSettingsManager;
	
	public DeleteOutputMetadataTask() {
	}
	
	@Override
	protected long countTotalItems() {
		return 10;
	}
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		
		workspaceSettingsManager.delete( workspace );
		incrementItemsProcessed();
		
		// delete calc steps
		processingChainManager.deleteCalculationSteps( workspace.getDefaultProcessingChain() );
		incrementItemsProcessed();
		
		// delete sampling design
		samplingDesignManager.deletePhase1Table( workspace );
		samplingDesignManager.deleteSamplingDesign( workspace );
		incrementItemsProcessed();
		
		samplingDesignManager.deleteStrata(workspace);
		incrementItemsProcessed();
		
		// delete ext equations
		equationManager.delete( workspace );
		incrementItemsProcessed();
		
		// delete output categories
		categoryManager.deleteOutputCategories( workspace );
		incrementItemsProcessed();
		
		// delete aois
		aoiManager.delete( workspace );
		incrementItemsProcessed();
		
		entityManager.resetPlotAreaScript( workspace );
		incrementItemsProcessed();
		
		//delete output variables
		variableManager.deleteUserDefinedVariables( workspace );
		incrementItemsProcessed();
		
		errorSettingsManager.delete( workspace );
		incrementItemsProcessed();
		
	}
	
	@Override
	public String getName() {
		return "Delete "  + getWorkspace().getName() + " calc metadata";
	}
	
}
