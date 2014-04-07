package org.openforis.calc;

import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.Job;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author G. Miceli
 */
public abstract class Calc {
	private Calc() {
		// Restrict sub-classing or instantiation
	}
	
//	public static void main(String[] args) {
//		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//		try {
//			CalculationEngine svc = ctx.getBean(CalculationEngine.class);
//			Job job = svc.runProcessingChain(21);
//			job.waitFor(15000);
////			CalcTest test = ctx.getBean(CalcTest.class);
////			test.testRunProcessingChain();
////			test.testREval();
//			ctx.registerShutdownHook();
//		} catch (Throwable e) {
//			e.printStackTrace();
//		} finally {
//			ctx.close();
//			System.exit(0);
//		}
//	}

}
