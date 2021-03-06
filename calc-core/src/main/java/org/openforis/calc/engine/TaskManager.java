package org.openforis.calc.engine;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.openforis.calc.chain.ProcessingChainTask;
import org.openforis.calc.chain.post.CreateAggregateTablesTask;
import org.openforis.calc.chain.post.PublishRolapSchemaTask;
import org.openforis.calc.chain.pre.AssignAoiColumnsTask;
import org.openforis.calc.collect.CollectDataImportTask;
import org.openforis.calc.collect.CollectMetadataImportTask;
import org.openforis.calc.collect.CollectSurveyImportJob;
import org.openforis.calc.collect.CreateInputSchemaTask;
import org.openforis.calc.collect.SpeciesImportTask;
import org.openforis.calc.module.ModuleRegistry;
import org.openforis.calc.schema.Schemas;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Manages execution of tasks and related locking and threads
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
@Component
public class TaskManager {

	@Autowired
	private PlatformTransactionManager txManager;

	@Autowired
	private Executor taskExecutor;

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private ModuleRegistry moduleRegistry;

	private Map<Integer, Job> jobs;
	private Map<Integer, SimpleLock> locks;

//	private Map<String, Job> jobsById;

	public TaskManager() {
		jobs = new HashMap<Integer, Job>();
		this.locks = new HashMap<Integer, SimpleLock>();
	}

	// TODO move to.. where??? REMOVE
	@Deprecated
	protected boolean isDebugMode() {
//		String mode = ((ConfigurableBeanFactory) beanFactory).resolveEmbeddedValue("${calc.debugMode}");
//		return "true".equals(mode);
		return false;
	}

	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing sampling design, variables)
	 */
//	@Deprecated
//	public CalcJob createCalcJob( Workspace workspace ) {
//		CalcJob job = new CalcJob(workspace, dataSource, this.beanFactory);
//		autowire(job);
//		return job;
//	}
	/**
	 * Create a job with tasks 
	 * @param workspace
	 * @param processingChain
	 * @return
	 */
//	@Deprecated
//	public CalcJob createDefaultCalcJob(Workspace workspace, boolean aggregates) {
//		CalcJob job = createCalcJob(workspace);
//		job.setAggregates(aggregates);
//		
//		ProcessingChain processingChain = workspace.getDefaultProcessingChain();
//		job.setProcessingChain( processingChain );
//		
//		return job;
//	}
	
	public Job createDefaultJob( Workspace workspace ) {
		Job job = createJob( workspace );
		
		addProcessingChainTasks(workspace, job);
		
		return job;
	}

	public void addProcessingChainTasks(Workspace workspace, Job job) {
		if( workspace.getDefaultProcessingChain().isCompleted() ){
			ProcessingChainTask chainTask = createTask( ProcessingChainTask.class );
			job.addTask( chainTask );
			
			CreateAggregateTablesTask aggTask 				= createTask( CreateAggregateTablesTask.class );
			job.addTask( aggTask );
			
			PublishRolapSchemaTask publishRolapSchemaTask 	= createTask( PublishRolapSchemaTask.class );
			job.addTask( publishRolapSchemaTask );
		}
	}
	
	public Job createPreProcessingJob(Workspace workspace) {
		Job job = createJob(workspace);
		
		addPreProcessingTasks(job);
		
		addProcessingChainTasks(workspace, job);
		
		return job;
	}

	public void addPreProcessingTasks(Job job) {
//		CalculateSamplingUnitWeightTask weightTask = new CalculateSamplingUnitWeightTask( job.newREnvironment() );
//		autowire(weightTask);
		
//		job.addTask( weightTask );
		job.addTask( createTask(AssignAoiColumnsTask.class) );
//		job.addTask( createTask(CalculateExpansionFactorsTask.class) );
	}
	
	/**
	 * Create collect data import job
	 * @param workspace
	 * @param survey
	 * @param backupFile
	 * @return
	 */
	public CollectSurveyImportJob createCollectSurveyImportJob( Workspace workspace, CollectSurvey survey , File backupFile ) {
		CollectSurveyImportJob job = new CollectSurveyImportJob(workspace, getDataSource(), survey);
		job.setSchemas( new Schemas(workspace) );
		autowire(job);
		
		CollectMetadataImportTask importTask = createTask(CollectMetadataImportTask.class);
		importTask.setBackupFile(backupFile);
		job.addTask(importTask);

		CreateInputSchemaTask schemaCreatorTask = createTask(CreateInputSchemaTask.class);
		job.addTask(schemaCreatorTask);

		SpeciesImportTask speciesImportTask = createTask(SpeciesImportTask.class);
		speciesImportTask.setBackupFile(backupFile);
		job.addTask(speciesImportTask);
		
		CollectDataImportTask dataImportTask = createTask(CollectDataImportTask.class);
		dataImportTask.setDataFile(backupFile);
		dataImportTask.setStep(Step.ANALYSIS);
		job.addTask(dataImportTask);
		
		if( workspace.hasSamplingDesign() ) {
			addPreProcessingTasks( job );
		}
		addProcessingChainTasks(workspace, job);

		return job;
	}
	
	/**
	 * Creates a job for testing a {@link CalculationStep}
	 */
//	@Deprecated
//	public CalcTestJob createCalcTestJob(Workspace workspace, CalculationStep step, ParameterMap variableParameters) throws InvalidProcessingChainException {
//		CalcTestJob job = new CalcTestJob(workspace, this.beanFactory, variableParameters);
//		((AutowireCapableBeanFactory) beanFactory).autowireBean(job);
//		return job;
//	}

	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing sampling design, variables)
	 */
	public Job createJob(Workspace workspace) {
		Job job = new Job(workspace, dataSource);
//		job.setDebugMode(isDebugMode());
		job.setSchemas(new Schemas(workspace));
		autowire(job);
		return job;
	}

	public <T extends Task> T createTask(Class<T> type) {
		try {
			T task = type.newInstance();
			autowire(task);
			return task;
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Invalid task " + type.getClass(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Invalid task " + type.getClass(), e);
		}
	}

	protected <T extends Object> void autowire(T object) {
		((AutowireCapableBeanFactory) beanFactory).autowireBean(object);
	}

//	public List<Task> createCalculationStepTasks(ProcessingChain chain) throws InvalidProcessingChainException {
//		List<Task> tasks = new ArrayList<Task>();
//		List<CalculationStep> steps = chain.getCalculationSteps();
//		for (CalculationStep step : steps) {
//			CalculationStepTask task = createCalculationStepTask(step);
//			tasks.add(task);
//		}
//		return tasks;
//	}

//	public CalculationStepTask createCalculationStepTask(CalculationStep step) throws InvalidProcessingChainException {
//		Operation<?> operation = moduleRegistry.getOperation(step);
//		if (operation == null) {
//			throw new InvalidProcessingChainException("Unknown operation in step " + step);
//		}
//		Class<? extends CalculationStepTask> taskType = operation.getTaskType();
//		CalculationStepTask task = createTask(taskType);
//		task.setCalculationStep(step);
//		return task;
//	}

	/**
	 * Executes a job in the background
	 * 
	 * @param job
	 */
	synchronized public void startJob(final Job job) throws WorkspaceLockedException {
		job.init();
		final Workspace ws = job.getWorkspace();
		final SimpleLock lock = lock( ws.getId() );
		
		jobs.put(ws.getId(), job);
//		jobsById.put(job.getId().toString(), job);
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					job.run();
				} finally {
					lock.unlock();
				}
			}
		});
	}

	synchronized public Job getJob(int workspaceId) {
		Job job = jobs.get(workspaceId);
		return job;
	}

//	synchronized public Job getJobById(String id) {
//		Job job = jobsById.get(id);
//		return job;
//	}

//	@SuppressWarnings("unchecked")
//	public List<Task> createTasks(Class<?>... types) {
//		List<Task> tasks = new ArrayList<Task>();
//		for (Class<?> type : types) {
//			Task task = createTask((Class<Task>) type);
//			tasks.add(task);
//		}
//		return tasks;
//	}

	protected DataSource getDataSource() {
		return dataSource;
	}
	
	synchronized public SimpleLock lock(int workspaceId) throws WorkspaceLockedException {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			lock = new SimpleLock();
			locks.put(workspaceId, lock);
		}
		if ( !lock.tryLock() ) {
			throw new WorkspaceLockedException();
		}
		return lock;
	}

	synchronized public boolean isLocked(int workspaceId) {
		SimpleLock lock = locks.get(workspaceId);
		if (lock == null) {
			return false;
		} else {
			return lock.isLocked();
		}
	}

//	public static void main(String[] args) {
//		String pattern = "###,##0.00######";
//		double value = 0.00021691812253467514;
//		
//		 DecimalFormat myFormatter = new DecimalFormat(pattern);
//	      String output = myFormatter.format(value);
//      System.out.println(value + "  " + pattern + "  " + output);
//	}
	
}
