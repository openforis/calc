package org.openforis.calc.chain.pre;

import org.junit.Test;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.Job;
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
	
	@Test
	public void testRun() throws WorkspaceLockedException, InvalidProcessingChainException {
		Job job = calculationEngine.runProcessingChain(21);
		job.waitFor(5000);
	}
}
