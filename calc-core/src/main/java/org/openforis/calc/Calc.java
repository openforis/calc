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
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			CalcTest test = ctx.getBean(CalcTest.class);
			test.test();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
