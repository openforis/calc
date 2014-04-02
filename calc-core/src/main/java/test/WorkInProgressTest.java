package test;

//import java.util.List;

import org.junit.Test;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChainDao;
import org.openforis.calc.engine.CalcJob;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 * @author M. Togna
 */
@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class WorkInProgressTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@Autowired
	private CalculationEngine calculationEngine;
	@Autowired
	private ProcessingChainDao processingChainDao;
	@Autowired
	private TaskManager taskManager;
	@Autowired
	private CalculationStepDao calculationStepDao;
	@Autowired
	private WorkspaceService workspaceService;
	

	@Test
	public void testStep() throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.fetchActiveWorkspace();
		
		
		CalcJob job = taskManager.createCalcJob(workspace);
		job.addCalculationStep(calculationStepDao.find(3));
//		job.addCalculationStep(calculationStepDao.find(8));
		job.init();
		
		System.out.println( job.toString() );
//		CustomRTask task = (CustomRTask) taskManager.createCalculationStepTask(step);
//		task.setMaxItems(18000);
//		
//		Job job = taskManager.createJob(workspace);
//		job.addTask(task);
//		
		taskManager.startJob(job);
		job.waitFor(5000);
		while(!job.isEnded()){
			//wait
		}
//		System.out.println(task.getItemsProcessed());
	}
}
