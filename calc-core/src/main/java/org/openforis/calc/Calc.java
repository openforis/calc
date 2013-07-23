package org.openforis.calc;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author G. Miceli
 */
public abstract class Calc {
	private Calc() {
		// Restrict sub-classing or instantiation
	}
	
	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		try {
			CalcTest test = ctx.getBean(CalcTest.class);
			test.testRunProcessingChain();
//			test.testREval();
			ctx.registerShutdownHook();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			ctx.close();
			System.exit(0);
		}
	}

}
