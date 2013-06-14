package org.openforis.calc;

import java.util.Collection;
import java.util.List;

import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.ProcessingChain;
import org.openforis.calc.engine.ProcessingChainService;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.ParameterHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalcTest {
	@Autowired
	private WorkspaceService workspaceService;
	
	@Autowired
	private ProcessingChainService pcs;
	
	public void test() {
		try {
			Workspace w = workspaceService.getWorkspace(1);
			System.out.println(w);
			Collection<Entity> es = w.getEntities();
			System.out.println("Entities:");
			for (Entity entity : es) {
				System.out.println(entity + " <- " + entity.getWorkspace());
			}
			System.out.println("Chains:");
			
			ProcessingChain newChain = new ProcessingChain();
			newChain.setName("Test Chain");
			newChain.parameters().setNumber("p", 1);
			
			ParameterMap innerMap = new ParameterHashMap();
			innerMap.setString("name", "Gino");
			newChain.parameters().setMap("inner", innerMap);
			w.addProcessingChain(newChain);
			pcs.saveProcessingChain(newChain);
			
			System.out.println(newChain.getId());
			
			
//			List<ProcessingChain> chains = w.getProcessingChains();
//			System.out.println("found "+chains.size()+" chains" );
//			for ( ProcessingChain chain : chains ) {
//				System.out.println(chain.getId());
//			}
			
//			List<ProcessingChain> chains = w.getProcessingChains();
//			for (ProcessingChain chain : chains) {
//				System.out.println(chain + " Params: "+chain.parameters());
//				chain.parameters().setString("sql", "DAJE!");
//			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
