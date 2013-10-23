/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.module.r.CustomRTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

//	@RequestMapping(value = "/{id}/nextResult.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public @ResponseBody
//	synchronized DataRecord getNextResult(@PathVariable String id) throws InvalidProcessingChainException, WorkspaceLockedException {
//		Job job = taskManager.getJobById(id);
//		CustomRTask task = (CustomRTask) job.tasks().get(0);
//		DataRecord dataRecord = task.getNextResult();
//		return dataRecord;
//	}

	@RequestMapping(value = "/{id}/results.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized List<DataRecord> getResults(@PathVariable String id, @RequestParam int offset, @RequestParam int numberOfRows) throws InvalidProcessingChainException, WorkspaceLockedException {
		Job job = taskManager.getJobById(id);
		CustomRTask task = (CustomRTask) job.tasks().get(0);
		List<DataRecord> results = task.getResults(offset, offset + numberOfRows );
		return results;
	}
	
}
