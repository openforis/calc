/**
 * 
 */
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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Mino Togna
 *
 */
public class ImportOutputMetadataTask extends Task {
	
	@JsonIgnore
	private WorkspaceBackup workspaceBackup;
	
	@Autowired
	private SamplingDesignManager samplingDesignManager;	
	@Autowired
	private AoiManager aoiManager;
	@Autowired
	private CategoryManager categoryManager;
	@Autowired
	private EquationManager equationManager;
	@Autowired
	private EntityManager entityManager;
	@Autowired
	private VariableManager variableManager;
	@Autowired
	private ProcessingChainManager processingChainManager;
	@Autowired
	private ErrorSettingsManager errorSettingsManager;
	@Autowired
	private WorkspaceService workspaceService;
	@Autowired
	private WorkspaceSettingsManager workspaceSettingsManager;
	@Autowired
	private Psql psql;
	
	
	@Override
	protected long countTotalItems() {
		return 14;
	}
	
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		
		//the order matters
		samplingDesignManager.importBackupPhase1Data( workspace, workspaceBackup );
		incrementItemsProcessed();

		samplingDesignManager.importBackupPrimarySUData( workspace, workspaceBackup );
		incrementItemsProcessed();

		workspaceSettingsManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		 
	 	samplingDesignManager.importBackupStrata( workspace , workspaceBackup );
	 	incrementItemsProcessed();
	 	
	 	entityManager.importBackup( workspace , workspaceBackup  );
	 	incrementItemsProcessed();
	 	
		categoryManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		equationManager.importBackup( workspace , workspaceBackup );
		incrementItemsProcessed();
		
		samplingDesignManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		aoiManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		samplingDesignManager.importBackupStrataAois( workspace , workspaceBackup );
	 	incrementItemsProcessed();
		
		variableManager.importBackup( workspace , workspaceBackup );
		incrementItemsProcessed();
		
		processingChainManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		errorSettingsManager.importBackup( workspace, workspaceBackup );
		incrementItemsProcessed();
		
		// reload workspace
		updateWorkspace(workspace);
		incrementItemsProcessed();
	}

	private void updateWorkspace( Workspace workspace ) {
//		workspace = workspaceService.fetchByCollectSurveyUri( workspace.getCollectSurveyUri() );
		workspace = workspaceService.get( workspace.getId() );
		workspaceService.resetResults(workspace);
		getJob().setWorkspace( workspace );
		getJob().setSchemas( workspace.schemas() );
	}

	void setWorkspaceBackup( WorkspaceBackup workspaceBackup ) {
		this.workspaceBackup = workspaceBackup;
	}
	
	@Override
	public String getName() {
		return "Import "  + getWorkspace().getName() + " calc metadata";
	}
}
