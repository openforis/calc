package org.openforis.calc.engine;

import org.springframework.stereotype.Service;
//import org.openforis.calc.chain.ProcessingChainDao;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Service
@Deprecated
public class CalculationEngine {
	
//	@Autowired 
//	private TaskManager taskManager;
//	
//	@Autowired
//	private ProcessingChainDao processingChainDao;
//	
//	@Autowired
//	private WorkspaceDao workspaceDao;
//
//	public static final Class<?>[] PREPROCESSING_TASKS = {
//			ResetOutputSchemaTask.class,
//			CreateCategoryDimensionTablesTask.class,
//			CreateAoiDimensionTablesTask.class,
//			CreateStratumDimensionTableTask.class,
//			CreateOutputTablesTask.class,
//			ApplyDefaultsTask.class,
//			AssignLocationColumnsTask.class,
//			AssignAoiColumnsTask.class
//		};
//
//	public static final Class<?>[] POSTPROCESSING_TASKS = {
//			CreateFactTablesTask.class,
//			AssignStratumTask.class,
//			AssignDimensionIdsTask.class,
//			CalculateExpansionFactorsTask.class,
//			CreateAggregateTablesTask.class,
//			PublishRolapSchemaTask.class
//		};
//
//	synchronized
//	public Job runProcessingChain(int chainId) throws WorkspaceLockedException, InvalidProcessingChainException {
//		Job job = createProcessingChainJob(chainId);
//		taskManager.startJob(job);
//		return job;
//	}
//
//	synchronized	
//	private Job createProcessingChainJob(int chainId) throws InvalidProcessingChainException {
//		ProcessingChain chain = processingChainDao.find(chainId);
//		if ( chain == null ) {
//			throw new IllegalArgumentException("No processing chain with id "+chainId);
//		}
//		Workspace workspace = chain.getWorkspace();
//		Job job = taskManager.createJob(workspace);
//		
//		// Add preprocessing tasks
//		job.addTasks( taskManager.createTasks(PREPROCESSING_TASKS) );
//		
//		// Add steps to job
//		job.addTasks( taskManager.createCalculationStepTasks(chain) );
//		
//		// Add preprocessing tasks
//		job.addTasks( taskManager.createTasks(POSTPROCESSING_TASKS) );
//		
//		return job;
//	}
//
//	synchronized
//	public Job updateStratumWeights(int workspaceId) throws WorkspaceLockedException {
//		Workspace workspace = workspaceDao.find(workspaceId);
//		Job job = taskManager.createJob(workspace);
//		job.addTask(taskManager.createTask(UpdateSamplingUnitAoisTask.class));
//		taskManager.startJob(job);
//		return job;
//	}
}
