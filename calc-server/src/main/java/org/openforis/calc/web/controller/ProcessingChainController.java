/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.openforis.calc.engine.InvalidProcessingChainException;
import org.openforis.calc.engine.ProcessingChainJob;
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
public class ProcessingChainController {

	@Autowired
	private WorkspaceManager workspaceManager;

	@Autowired
	private ProcessingChainService processingChainService;

	@RequestMapping(value = "/workspaces.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	List<Workspace> getWorkspaces() {
		List<Workspace> workspaces = workspaceManager.loadAll();
		return workspaces;
	}

	@RequestMapping(value = "/workspaces/chains/{chainId}/jobs.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	ProcessingChainJob getProcessingChainJob(@PathVariable int chainId) throws InvalidProcessingChainException {
		ProcessingChainJob processingChainJob = getProcessingChain(chainId);
		return processingChainJob;
	}

	private ProcessingChainJob getProcessingChain(int chainId) throws InvalidProcessingChainException {
		ProcessingChainJob processingChainJob = processingChainService.getProcessingChainJob(chainId);
		return processingChainJob;
	}

	@RequestMapping(value = "/workspaces/chains/{chainId}/run.json", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody
	ProcessingChainJob runProcessingChain(@PathVariable int chainId, @RequestParam(value = "taskIds", required = false) String taskIds) throws InvalidProcessingChainException, WorkspaceLockedException {
		System.out.println("executing chain " + chainId);

		Set<UUID> scheduledTasks = new HashSet<UUID>();
		for ( String taskId : taskIds.split(",") ) {
			UUID uuid = UUID.fromString(taskId);
			scheduledTasks.add(uuid);
		}

		ProcessingChainJob processingChainJob = getProcessingChain(chainId);
		processingChainJob.setScheduledTasks(scheduledTasks);

		processingChainService.startProcessingChainJob(chainId, scheduledTasks);

		return getProcessingChain(chainId);
	}

}
