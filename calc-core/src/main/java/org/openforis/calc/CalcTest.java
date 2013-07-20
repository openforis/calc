package org.openforis.calc;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.openforis.calc.engine.CalculationStep;
import org.openforis.calc.engine.CalculationStepManager;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Module;
import org.openforis.calc.engine.ModuleRegistry;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.ProcessingChain;
import org.openforis.calc.engine.ProcessingChainService;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceManager;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.persistence.ParameterHashMap;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalcTest {
	@Autowired
	private WorkspaceManager workspaceService;

	@Autowired
	private ProcessingChainService pcs;

	@Autowired
	private CalculationStepManager calcStepService;

	@Autowired
	private TaskManager taskManager;

	@Autowired
	private ModuleRegistry moduleRegistry;

	@Autowired
	private ProcessingChainService processingChainService;

	@Autowired
	private R r;
	
	public void testModules() {
		Set<Module> modules = moduleRegistry.getModules();
		for (Module module : modules) {
			System.out.println(module);
		}
	}

	public void createAndSave() {
		Workspace w = workspaceService.get(1);
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
		step.parameters().setString("sql", "select * from calc.aoi");

		newChain.addCalculationStep(step);
		w.addProcessingChain(newChain);
	}

	public void testRunProcessingChain() throws Throwable {
		Workspace ws = workspaceService.get(1);
		ProcessingChain chain = ws.getProcessingChains().get(0);
		int chainId = chain.getId();
		Job job = processingChainService.getProcessingChainJob(chainId);
		Set<UUID> taskIds = job.getTaskIds();
		processingChainService.startProcessingChainJob(chainId, taskIds);
		while (!job.isEnded()) {
			System.out.println(job.getStatus());
			Thread.sleep(1000);
		}
		System.out.println("DONE!");
	}

	public void testREval() throws Throwable {
		REnvironment renv1 = r.newEnvironment();
		renv1.eval("a=runif(1)");
//        System.out.println(renv1.evalDouble("runif(1)"));
        renv1.eval("print(a)");
        
		REnvironment renv2 = r.newEnvironment();
        renv2.eval("print(a)");
	}
}
