/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.engine.CalculationStep;
import org.openforis.calc.engine.CalculationStepManager;
import org.openforis.calc.engine.InvalidProcessingChainException;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Module;
import org.openforis.calc.engine.ModuleRegistry;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.ProcessingChain;
import org.openforis.calc.engine.ProcessingChainManager;
import org.openforis.calc.engine.ProcessingChainService;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest")
public class ProcessingChainRestController {

	@Autowired
	private WorkspaceManager workspaceManager;

	@Autowired
	private ProcessingChainService processingChainService;

	@Autowired
	private ProcessingChainManager processingChainManager;

	@Autowired
	private CalculationStepManager calculationStepManager;

	@Autowired
	private ModuleRegistry moduleRegistry;

	@RequestMapping(value = "/workspaces.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	List<Workspace> getWorkspaces() {
		List<Workspace> workspaces = workspaceManager.loadAll();
		return workspaces;
	}

//	@Deprecated
	@RequestMapping(value = "/workspaces/chains/{chainId}/job.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Job getProcessingChainJob(@PathVariable int chainId) throws InvalidProcessingChainException {
		Job processingChainJob = processingChainService.getProcessingChainJob(chainId);
		return processingChainJob;
	}

	@RequestMapping(value = "/workspaces/chains/{chainId}/run.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Job runProcessingChain(@PathVariable int chainId, @RequestParam(value = "taskIds", required = false) String taskIds) throws InvalidProcessingChainException,
			WorkspaceLockedException {
		System.out.println("executing chain " + chainId);

		Set<UUID> scheduledTasks = new HashSet<UUID>();
		String[] ids = taskIds.split(",");
		for ( String taskId : ids ) {
			UUID uuid = UUID.fromString(taskId);
			scheduledTasks.add(uuid);
		}
		Job processingChainJob = processingChainService.getProcessingChainJob(chainId);

		processingChainJob.setScheduledTasks(scheduledTasks);

		processingChainService.startProcessingChainJob(chainId, scheduledTasks);

		processingChainJob = processingChainService.getProcessingChainJob(chainId);
		return processingChainJob;
	}

	// rest/workspaces/chains.json
	/**
	 * It creates a new processing chain with the given name and returns the workspace it belongs to
	 * 
	 * @param name
	 *            the processing chain name
	 * @param wsId
	 *            the workspace id where to save the processing chain
	 * @return
	 */
	@RequestMapping(value = "/workspaces/chains.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Workspace createProcessingChain(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "wsId", required = true) int wsId) {
		Workspace workspace = workspaceManager.get(wsId);

		ProcessingChain chain = new ProcessingChain();
		chain.setName(name);
		chain.setWorkspace(workspace);

		processingChainManager.save(chain);

		workspace = workspaceManager.get(wsId);
		return workspace;
	}

	/**
	 * Updates the processing chain name
	 * 
	 * @param name
	 * @param chainId
	 * @return the updated processing chain
	 */
	@RequestMapping(value = "/workspaces/chains.json", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody
	ProcessingChain updateProcessingChain(@RequestParam(value = "name", required = true) String name, @RequestParam(value = "chainId", required = true) int chainId) {
		// ProcessingChain updateProcessingChain(HttpServletRequest request) {
		ProcessingChain chain = processingChainManager.get(chainId);
		chain.setName(name);

		processingChainManager.save(chain);

		return processingChainManager.get(chainId);
	}

	/**
	 * Reorder the calculation steps as they appear in the stepIds argument (separated by comma)
	 * 
	 * @param chainId
	 * @param stepIds
	 * @return
	 */
	@RequestMapping(value = "/workspaces/chains/{chainId}/steps/no.json", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody
	ProcessingChain updateStepsOrder(@PathVariable int chainId, @RequestParam(value = "stepIds", required = true) String stepIds) {
		ProcessingChain chain = processingChainManager.get(chainId);
		String[] ids = stepIds.split(",");
		int i = 1;
		for ( String stepId : ids ) {
			int id = Integer.valueOf(stepId);
			CalculationStep step = chain.getCalculationStep(id);
			step.setStepNo(i);

			i++;
		}

		calculationStepManager.saveAll(chain.getCalculationSteps());

		chain = processingChainManager.get(chainId);
		return chain;
	}

	@RequestMapping(value = "/modules.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	Collection<Module> getModules() {
		return moduleRegistry.getModules();
	}

	@RequestMapping(value = "/workspaces/chains/{chainId}/steps/step.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	ProcessingChain createCalculationStep(
			@PathVariable int chainId, 
			@RequestParam(value = "stepNo", required = true) int stepNo,
			@RequestParam(value = "moduleName", required = true) String moduleName,
			@RequestParam(value = "moduleVersion", required = true) String moduleVersion,
			@RequestParam(value = "operationName", required = true) String operationName,
			@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "parameters", required = true) String parameters 
			) throws ParseException
	{
		ProcessingChain chain = processingChainManager.get(chainId);
		
		CalculationStep step = new CalculationStep();
		step.setProcessingChain(chain);
		step.setStepNo(stepNo);
		step.setModuleName(moduleName);
		step.setModuleVersion(moduleVersion);
		step.setOperationName(operationName);
		step.setName(name);
		
		ParameterMap parameterMap = step.parameters();		
		JSONParser jsonParser = new JSONParser();
		JSONObject json = (JSONObject) jsonParser.parse(parameters);
		for ( Object paramName : json.keySet() ) {
			Object paramValue = json.get(paramName);
			parameterMap.setString(paramName.toString(), paramValue.toString() );
		}

		//save the calculation step
		calculationStepManager.save(step);
		//reload and returns the chain
		chain = processingChainManager.get(chainId);
		return chain;
	}

	
	@RequestMapping(value = "/workspaces/chains/{chainId}/steps/step/{stepId}.json", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody
	ProcessingChain updateCalculationStep(
			@PathVariable int chainId,
			@PathVariable int stepId, 
			@RequestParam(value = "stepNo", required = true) int stepNo,
			@RequestParam(value = "moduleName", required = true) String moduleName,
			@RequestParam(value = "moduleVersion", required = true) String moduleVersion,
			@RequestParam(value = "operationName", required = true) String operationName,
			@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "parameters", required = true) String parameters 
			) throws ParseException
	{
		ProcessingChain chain = processingChainManager.get(chainId);
		
		CalculationStep step =  calculationStepManager.get(stepId); //  new CalculationStep();
//		step.setProcessingChain(chain);
		step.setStepNo(stepNo);
		step.setModuleName(moduleName);
		step.setModuleVersion(moduleVersion);
		step.setOperationName(operationName);
		step.setName(name);
		
		ParameterMap parameterMap = step.parameters();		
		JSONParser jsonParser = new JSONParser();
		JSONObject json = (JSONObject) jsonParser.parse(parameters);
		for ( Object paramName : json.keySet() ) {
			Object paramValue = json.get(paramName);
			parameterMap.setString(paramName.toString(), paramValue.toString() );
		}

		//save the calculation step
		calculationStepManager.save(step);
		//reload and returns the chain
		chain = processingChainManager.get(chainId);
		return chain;
	}
}
