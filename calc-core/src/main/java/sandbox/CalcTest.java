package sandbox;

import java.util.Collection;
import java.util.Set;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.CalculationEngine;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.ProcessingChainService;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.module.Module;
import org.openforis.calc.module.ModuleRegistry;
import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CalcTest {
	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private ModuleRegistry moduleRegistry;

	@Autowired
	private ProcessingChainService processingChainService;

	@Autowired
	private CalculationEngine calculationEngine;

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
		newChain.setCaption("Test Chain");
		newChain.parameters().setNumber("p", 1);

		ParameterMap innerMap = new ParameterHashMap();
		innerMap.setString("name", "Gino");
		newChain.parameters().setMap("inner", innerMap);

		CalculationStep step = new CalculationStep();
		step.setCaption("Step 1");
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
//		Job job = processingChainService.getProcessingChainJob(chainId);
//		Set<UUID> taskIds = job.getTaskIds();
		Job job = calculationEngine.runProcessingChain(chainId/*, taskIds*/);
		job.waitFor(30000);
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
