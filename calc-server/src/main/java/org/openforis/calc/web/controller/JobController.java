/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.module.r.CustomRTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Mino Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/job")
public class JobController {
	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private CalculationStepDao calculationStepDao;

	@Autowired
	private TaskManager taskManager;

	/**
	 * Returns the job associated with the stepId if present for the current
	 * workspace
	 * 
	 * @param stepId
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/{id}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job getJob(@PathVariable String id) throws InvalidProcessingChainException, WorkspaceLockedException {
		Job job = taskManager.getJobById(id);

		return job;
	}

	/**
	 * Returns the results for a job
	 * 
	 * @param id
	 * @param offset
	 * @param numberOfRows
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/{id}/results.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized List<DataRecord> getResults(@PathVariable String id, @RequestParam int offset, @RequestParam(required = false) Integer numberOfRows) throws InvalidProcessingChainException,
			WorkspaceLockedException {
		Job job = taskManager.getJobById(id);
		CustomRTask task = (CustomRTask) job.tasks().get(0);
		if (numberOfRows == null) {
			numberOfRows = (int) task.getItemsProcessed();
		}
		List<DataRecord> results = task.getResults(offset, offset + numberOfRows);
		return results;
	}

	/**
	 * Execute a job for the given calculation step id
	 * 
	 * @param stepId
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/step/{stepId}/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job executeCalculationStep(@PathVariable int stepId) throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();

		CalculationStep step = calculationStepDao.find(stepId);
		CustomRTask task = (CustomRTask) taskManager.createCalculationStepTask(step);

		Job job = taskManager.createJob(workspace);
		job.addTask(task);

		taskManager.startJob(job);

		return job;
	}

	/**
	 * Execute a job for the given calculation step id
	 * 
	 * @param stepId
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/step/{stepId}/test.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job testCalculationStep(@PathVariable int stepId, @RequestBody HashMap<String, String> parameters) throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		/*
		CalculationStep step = calculationStepDao.find(stepId);
		CustomRTask task = (CustomRTask) taskManager.createCalculationStepTask(step);

		Job job = taskManager.createJob(workspace);
		job.addTask(task);

		taskManager.startJob(job);

		return job;
		*/
		return null;
	}

}
