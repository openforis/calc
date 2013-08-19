package org.openforis.calc.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.openforis.calc.rolap.RolapSchema;
import org.openforis.calc.rolap.RolapSchemaFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
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
	private WorkspaceService workspaceManager;
	
	@Autowired 
	private BeanFactory beanFactory;
	
	@Autowired
	private DataSource userDataSource;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private RolapSchemaFactory rolapSchemaFactory;

	@Autowired
	private PropertyPlaceholderConfigurer placeholderConfigurer;
	
	private Map<Integer, Job> jobs;
	
	public TaskManager() {
		this.jobs = new HashMap<Integer, Job>();
	}

	// TODO move to.. where?
	private boolean isDebugMode() {
		String mode = ((ConfigurableBeanFactory)beanFactory).resolveEmbeddedValue("${calc.debugMode}");
		return "true".equals(mode);
	}
	
	/**
	 * Create a job with write-access to the output schema and read-only access to the
	 * calc and input schemas.  Used when running processing chains.
	 */
	public Job createUserJob(Workspace workspace) {
		RolapSchema rolapSchema = rolapSchemaFactory.createRolapSchema(workspace);		

		return new Job(workspace, isDebugMode(), userDataSource, rolapSchema);
	}
	
	/**
	 * Create a job with write-access to the calc schema. Used for updating
	 * metadata (e.g. importing sampling design, variables)  
	 */
	public Job createSystemJob(Workspace workspace){
		return new Job(workspace, isDebugMode(), dataSource, null);
	}

	public <T extends Task> T createTask(Class<T> type) {
		try {
			T task = type.newInstance();
			((AutowireCapableBeanFactory)beanFactory).autowireBean(task);
			return task;
		} catch ( InstantiationException e ) {
			throw new IllegalArgumentException("Invalid task " + type.getClass(), e);
		} catch ( IllegalAccessException e ) {
			throw new IllegalArgumentException("Invalid task " + type.getClass(), e);
		}
	}

	/**
	 * Executes a job in the background
	 * 
	 * @param job
	 */
	synchronized
	public void startJob(final Job job) throws WorkspaceLockedException {
		final Workspace ws = job.getWorkspace();
		final SimpleLock lock = workspaceManager.lock(ws.getId());
		jobs.put(ws.getId(), job);
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

	@SuppressWarnings("unchecked")
	public List<Task> createTasks(Class<?>... types) {
		List<Task> tasks = new ArrayList<Task>();
		for (Class<?> type : types) {
			Task task = createTask((Class<Task>) type);
			tasks.add(task);
		}
		return tasks;
	}
}
