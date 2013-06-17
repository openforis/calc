package org.openforis.calc;

import java.util.Collection;

import javax.sql.DataSource;

import org.openforis.calc.engine.CalculationStep;
import org.openforis.calc.engine.CalculationStepService;
import org.openforis.calc.engine.Context;
import org.openforis.calc.engine.Module;
import org.openforis.calc.engine.ModuleRegistry;
import org.openforis.calc.engine.Operation;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.ProcessingChain;
import org.openforis.calc.engine.ProcessingChainService;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.TaskManager;
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

	@Autowired
	private CalculationStepService calcStepService;
	
	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private ModuleRegistry moduleRegistry;
	
	@Autowired
	private DataSource dataSource;
//	public void test() {
//		Set<Module> modules = moduleRegistry.getModules();
//		for (Module module : modules) {
//			System.out.println(module);
//		}
//	}
	
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
			
			CalculationStep step = new CalculationStep();
			step.setName("Step 1");
			step.setStepNo(1);
			step.setModuleName("calc-sql");
			step.setModuleVersion("1.0");
			step.setOperationName("exec-sql");
			step.parameters().setString("sql", "select 1000");

			newChain.addCalculationStep(step);
			w.addProcessingChain(newChain);		
			
			
			// Where should this code be?
			Context context = new Context(w, dataSource);
			Module module = moduleRegistry.getModule(step.getModuleName(), step.getModuleVersion());
			Operation<?> operation = module.getOperation(step.getOperationName());
			Task task = operation.createTask(context, step.parameters());
			taskManager.start(task);
			//
			
//			workspaceService.saveWorkspace(w);
//			pcs.saveProcessingChain(newChain);
//			calcStepService.saveCalculationStep(step);
			
			
			
//			System.out.println("New: "+newChain);
//			System.out.println("---------------");
//			step.setOperationName("exec-r");
			
//			calcStepService.saveCalculationStep(step);
//			workspaceService.saveWorkspace(w);
//			System.out.println("Saved: "+newChain);
			
			
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
