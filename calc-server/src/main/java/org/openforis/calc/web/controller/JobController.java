/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.engine.CalcJob;
import org.openforis.calc.engine.CalcTestJob;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.json.ParameterMapJsonParser;
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
//	@RequestMapping(value = "/{id}.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public @ResponseBody
//	synchronized Job getJob(@PathVariable String id) throws InvalidProcessingChainException, WorkspaceLockedException {
//		Job job = taskManager.getJobById(id);
//
//		return job;
//	}

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
//	@RequestMapping(value = "/{id}/results.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public @ResponseBody
//	synchronized List<DataRecord> getResults(@PathVariable String id, @RequestParam int offset, @RequestParam(required = false) Integer numberOfRows) throws InvalidProcessingChainException,
//			WorkspaceLockedException {
//		Job job = taskManager.getJobById(id);
//		CustomRTask task = (CustomRTask) job.tasks().get(0);
//		if (numberOfRows == null) {
//			numberOfRows = (int) task.getItemsProcessed();
//		}
//		List<DataRecord> results = task.getResults(offset, offset + numberOfRows);
//		return results;
//	}

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
		
		CalcJob job = taskManager.createCalcJob(workspace);
		job.addCalculationStep(step);
		job.setTempResults(true);
		
		taskManager.startJob(job);

		return job;
	}

	/**
	 * Execute defualt processing chain
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/execute.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job execute() throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();
		
		CalcJob job = taskManager.createDefaultCalcJob( workspace , true );
		
		taskManager.startJob(job);

		return job;
	}
	
	/**
	 * Creates a job that tests the calculation step with the given id
	 * 
	 * @param stepId
	 * @param variables Variable parameters in JSON format
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@RequestMapping(value = "/test/execute.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job testCalculationStep(@RequestParam int stepId, @RequestParam String variables) throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();

		CalculationStep step = calculationStepDao.find(stepId);
		ParameterMap parameterMap = new ParameterMapJsonParser().parse(variables);

		CalcTestJob job = taskManager.createCalcTestJob(workspace, step, parameterMap);
		job.setCalculationStep(step);
		
		taskManager.startJob(job);

		return job;
	}

	@RequestMapping(value = "/test/results.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> getTestResults(@RequestParam String jobId, @RequestParam int offset, @RequestParam(value = "numberOfRows", required=false) Integer numberOfRows) {
		CalcTestJob job = getTestJob(jobId);
		if ( numberOfRows == null ) {
			numberOfRows = 50;
		}
		List<DataRecord> results = job.getResults(offset, offset + numberOfRows);
		return results;
	}
	
	@RequestMapping(value = "/test/results/count.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response getTestResultsCount(@RequestParam String jobId) {
		Response response = new Response();
		CalcTestJob job = getTestJob(jobId);
		long count = job.getResultsCount();
		response.addField("count", count);
		return response;
	}
	
	private CalcTestJob getTestJob(String jobId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Job job = taskManager.getJob(workspace.getId());
		
		if( job == null || ! job.getId().toString().equals(jobId) ){
			throw new IllegalArgumentException("Job with id " + jobId + " not found for the active workspace");
		}
		return (CalcTestJob) job;
	}

}
