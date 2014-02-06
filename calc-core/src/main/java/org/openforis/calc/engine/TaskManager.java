package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.pre.AssignAoiColumnsTask;
import org.openforis.calc.chain.pre.CalculateExpansionFactorsTask;
import org.openforis.calc.module.ModuleRegistry;
import org.openforis.calc.module.Operation;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
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

//	private Map<String, Job> jobsById;

	public TaskManager() {
		jobs = new HashMap<Integer, Job>();
//		jobsById = new HashMap<String, Job>();
	}

	// TODO move to.. where?
	protected boolean isDebugMode() {
		String mode = ((ConfigurableBeanFactory) beanFactory).resolveEmbeddedValue("${calc.debugMode}");
		return "true".equals(mode);
	}

	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing sampling design, variables)
	 */
	public CalcJob createCalcJob(Workspace workspace) {
		CalcJob job = new CalcJob(workspace, dataSource, this.beanFactory);
		autowire(job);
		return job;
	}
	/**
	 * Create a job with tasks 
	 * @param workspace
	 * @param processingChain
	 * @return
	 */
	public CalcJob createDefaultCalcJob(Workspace workspace, boolean aggregates) {
		CalcJob job = new CalcJob(workspace, dataSource, this.beanFactory);
		
		ProcessingChain processingChain = workspace.getDefaultProcessingChain();
		List<CalculationStep> steps = processingChain.getCalculationSteps();
		job.addCalculationStep(steps);
		job.setAggregates(aggregates);
		
		autowire(job);
		
		return job;
	}
	
	public Job createPreProcessingJob(Workspace workspace) {
		Job job = createJob(workspace);
		
		CalculateSamplingUnitWeightTask weightTask = new CalculateSamplingUnitWeightTask( job.newREnvironment() );
		autowire(weightTask);
		
		job.addTask(weightTask);
		job.addTask( createTask(AssignAoiColumnsTask.class) );
		job.addTask( createTask(CalculateExpansionFactorsTask.class) );
		
		return job;
	}
	
	/**
	 * Creates a job for testing a {@link CalculationStep}
	 */
	public CalcTestJob createCalcTestJob(Workspace workspace, CalculationStep step, ParameterMap variableParameters) throws InvalidProcessingChainException {
		CalcTestJob job = new CalcTestJob(workspace, this.beanFactory, variableParameters);
		((AutowireCapableBeanFactory) beanFactory).autowireBean(job);
		return job;
	}

	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing sampling design, variables)
	 */
	public Job createJob(Workspace workspace) {
		Job job = new Job(workspace, dataSource);
		job.setDebugMode(isDebugMode());
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

	public List<Task> createCalculationStepTasks(ProcessingChain chain) throws InvalidProcessingChainException {
		List<Task> tasks = new ArrayList<Task>();
		List<CalculationStep> steps = chain.getCalculationSteps();
		for (CalculationStep step : steps) {
			CalculationStepTask task = createCalculationStepTask(step);
			tasks.add(task);
		}
		return tasks;
	}

	public CalculationStepTask createCalculationStepTask(CalculationStep step) throws InvalidProcessingChainException {
		Operation<?> operation = moduleRegistry.getOperation(step);
		if (operation == null) {
			throw new InvalidProcessingChainException("Unknown operation in step " + step);
		}
		Class<? extends CalculationStepTask> taskType = operation.getTaskType();
		CalculationStepTask task = createTask(taskType);
		task.setCalculationStep(step);
		return task;
	}

	/**
	 * Executes a job in the background
	 * 
	 * @param job
	 */
	synchronized public void startJob(final Job job) throws WorkspaceLockedException {
		job.init();
		final Workspace ws = job.getWorkspace();
		final SimpleLock lock = workspaceService.lock(ws.getId());
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

	@SuppressWarnings("unchecked")
	public List<Task> createTasks(Class<?>... types) {
		List<Task> tasks = new ArrayList<Task>();
		for (Class<?> type : types) {
			Task task = createTask((Class<Task>) type);
			tasks.add(task);
		}
		return tasks;
	}

	protected DataSource getDataSource() {
		return dataSource;
	}

}
