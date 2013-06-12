package org.openforis.calc;

import org.openforis.calc.workspace.Workspace;
import org.openforis.calc.workspace.WorkspaceManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author G. Miceli
 */
public abstract class Calc {
	private Calc() {
		// Do not allow subclassing or instantiation
	}
	
	public static void main(String[] args) {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			WorkspaceManager wmgr = ctx.getBean(WorkspaceManager.class);

//			String mdxFileName = ctx.getBeanFactory().resolveEmbeddedValue("${mdxOutputPath}");
			
			Workspace w = wmgr.getWorkspace(1);
			
			System.out.println(w);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

}
