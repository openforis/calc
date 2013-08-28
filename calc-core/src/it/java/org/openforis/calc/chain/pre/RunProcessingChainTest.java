package org.openforis.calc.chain.pre;

import java.util.List;

import org.junit.Test;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 *
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class RunProcessingChainTest extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	private CalculationEngine calculationEngine;
	@Autowired
	private ProcessingChainDao processingChainDao;
	@Autowired
	private TaskManager taskManager;

	//@Test
	public void testRunCalculationStepTasks() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
		Workspace workspace = chain.getWorkspace(); 
		Job job = taskManager.createUserJob(workspace);
		List<Task> tasks = taskManager.createCalculationStepTasks(chain);
		job.addTasks( tasks );
		taskManager.startJob(job);
		job.waitFor(5000);
	}
	
	@Test
	public void testRunProcessingChain() throws WorkspaceLockedException, InvalidProcessingChainException {
		ProcessingChain chain = processingChainDao.find(21);
//		Workspace workspace = chain.getWorkspace(); 
//		Job job = taskManager.createUserJob(workspace);
//		List<Task> tasks = taskManager.createCalculationStepTasks(chain);
//		job.addTasks( tasks );
//		taskManager.startJob(job);
//		job.waitFor(5000);
//		
		Integer chainId = chain.getId();
		Job job = calculationEngine.runProcessingChain(chainId);
		job.waitFor(15000);
	}
}
