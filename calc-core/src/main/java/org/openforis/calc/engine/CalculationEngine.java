package org.openforis.calc.engine;

import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.chain.post.AssignDimensionIdsTask;
import org.openforis.calc.chain.post.CalculateExpansionFactorsTask;
import org.openforis.calc.chain.post.CreateAggregateTablesTask;
import org.openforis.calc.chain.post.CreateFactTablesTask;
import org.openforis.calc.chain.post.PublishRolapSchemaTask;
import org.openforis.calc.chain.pre.ApplyDefaultsTask;
import org.openforis.calc.chain.pre.AssignAoiColumnsTask;
import org.openforis.calc.chain.pre.AssignLocationColumnsTask;
import org.openforis.calc.chain.pre.AssignStratumTask;
import org.openforis.calc.chain.pre.CreateAoiDimensionTablesTask;
import org.openforis.calc.chain.pre.CreateCategoryDimensionTablesTask;
import org.openforis.calc.chain.pre.CreateOutputTablesTask;
import org.openforis.calc.chain.pre.CreateStratumDimensionTableTask;
import org.openforis.calc.chain.pre.ResetOutputSchemaTask;
import org.openforis.calc.metadata.task.UpdateSamplingUnitAoisTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Service
public class CalculationEngine {
	
	@Autowired 
	private TaskManager taskManager;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired
	private WorkspaceDao workspaceDao;

	public static final Class<?>[] PREPROCESSING_TASKS = {
			ResetOutputSchemaTask.class,
			CreateCategoryDimensionTablesTask.class,
			CreateAoiDimensionTablesTask.class,
			CreateStratumDimensionTableTask.class,
			CreateOutputTablesTask.class,
			ApplyDefaultsTask.class,
			AssignLocationColumnsTask.class,
			AssignAoiColumnsTask.class
		};

	public static final Class<?>[] POSTPROCESSING_TASKS = {
			CreateFactTablesTask.class,
			AssignStratumTask.class,
			AssignDimensionIdsTask.class,
			CalculateExpansionFactorsTask.class,
			CreateAggregateTablesTask.class,
			PublishRolapSchemaTask.class
		};

	synchronized
	public Job runProcessingChain(int chainId) throws WorkspaceLockedException, InvalidProcessingChainException {
		Job job = createProcessingChainJob(chainId);
		taskManager.startJob(job);
		return job;
	}

	synchronized	
	private Job createProcessingChainJob(int chainId) throws InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(chainId);
		if ( chain == null ) {
			throw new IllegalArgumentException("No processing chain with id "+chainId);
		}
		Workspace workspace = chain.getWorkspace();
		Job job = taskManager.createJob(workspace);
		
		// Add preprocessing tasks
		job.addTasks( taskManager.createTasks(PREPROCESSING_TASKS) );
		
		// Add steps to job
		job.addTasks( taskManager.createCalculationStepTasks(chain) );
		
		// Add preprocessing tasks
		job.addTasks( taskManager.createTasks(POSTPROCESSING_TASKS) );
		
		return job;
	}

	synchronized
	public Job updateStratumWeights(int workspaceId) throws WorkspaceLockedException {
		Workspace workspace = workspaceDao.find(workspaceId);
		Job job = taskManager.createJob(workspace);
		job.addTask(taskManager.createTask(UpdateSamplingUnitAoisTask.class));
		taskManager.startJob(job);
		return job;
	}
}
