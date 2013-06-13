package org.openforis.calc;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.openforis.calc.metadata.Entity;
import org.openforis.calc.workspace.Workspace;
import org.openforis.calc.workspace.WorkspaceService;
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
//			EntityManagerFactory emf = ctx.getBean(EntityManagerFactory.class);
			WorkspaceService wmgr = ctx.getBean(WorkspaceService.class);
			Workspace w = wmgr.getWorkspace(1);
			System.out.println(w);
			Collection<Entity> es = w.getEntities();
			for (Entity entity : es) {
				System.out.println(entity + " <- " + entity.getWorkspace());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

}
