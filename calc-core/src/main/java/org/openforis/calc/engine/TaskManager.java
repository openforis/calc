package org.openforis.calc.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
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
	private AutowireCapableBeanFactory beanFactory;
	
	@Autowired
	private ApplicationContext applContext;

	@Autowired
	private DataSource userDataSource;
	
	private Map<Integer, Job> jobs;
	
	public TaskManager() {
		this.jobs = new HashMap<Integer, Job>();
	}
	
	public Job createJob(Workspace workspace) {
		JobContext context = new JobContext(workspace, userDataSource);
		Job job = applContext.getBean(Job.class);
		job.setContext(context);
		return job;
	}
	
	public <T extends Task> T createTask(Class<T> type) {
		try {
			T task = type.newInstance();
			beanFactory.autowireBean(task);
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
		final JobContext ctx = job.getContext();
		final Workspace ws = ctx.getWorkspace();
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
}
