package test;

import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author A. Sanchez-Paus Diaz
 * @author G. Miceli
 *
 */
//@ContextConfiguration(locations = { "classpath:applicationContext.xml" })
public class UpdateStratumWeightsTest { 
//extends AbstractTransactionalJUnit4SpringContextTests {
	@Autowired
	CalculationEngine calculationEngine;
	
//	@Test
	public void testRun() throws WorkspaceLockedException {
		Job job = calculationEngine.updateStratumWeights(1);
		job.waitFor(5000);
	}
}
