/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.CalculationStepService;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.CalculationStepTestTask;
import org.openforis.calc.engine.DataRecord;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.engine.CalculationStepTestTask.Parameters;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.module.r.CalcRModule;
import org.openforis.calc.module.r.CustomROperation;
import org.openforis.calc.module.r.CustomRTask;
import org.openforis.calc.web.form.CalculationStepForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/calculationstep")
public class CalculationStepController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private CalculationStepService calculationStepService;
	
	@Autowired
	private VariableDao variableDao;

	@Autowired
	private CalculationStepDao calculationStepDao;

	@Autowired
	private TaskManager taskManager;

	@RequestMapping(value = "/save.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response save(@Valid CalculationStepForm form, BindingResult result) {
		Response response = validate(form, result);
		if (!response.hasErrors()) {
			Workspace ws = workspaceService.getActiveWorkspace();
			ProcessingChain chain = ws.getDefaultProcessingChain();
			CalculationStep step;
			Integer stepId = form.getId();
			if (stepId == null) {
				step = new CalculationStep();
				step.setStepNo(chain.getNextStepNo());
			} else {
				step = chain.getCalculationStep(stepId);
			}
			Variable<?> outputVariable = variableDao.find(form.getVariableId());
			step.setOutputVariable(outputVariable);
			step.setModuleName(CalcRModule.MODULE_NAME);
			step.setModuleVersion(CalcRModule.VERSION_1);
			step.setOperationName(CustomROperation.NAME);
			step.setCaption(form.getCaption());
			step.setScript(form.getScript());
			chain.addCalculationStep(step);

			step = calculationStepDao.save(step);
			response.addField("calculationStep", step);
		}
		return response;
	}

	@RequestMapping(value = "/validate.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response validate(@Valid CalculationStepForm form, BindingResult result) {
		Response response = new Response(result.getAllErrors());
		return response;
	}

	@RequestMapping(value = "/load.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<CalculationStep> loadAll() {
		return calculationStepDao.loadAll("stepNo");
	}

	@RequestMapping(value = "/{stepId}/load.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	CalculationStep load(@PathVariable int stepId) {
		return calculationStepDao.find(stepId);
	}
	
	@RequestMapping(value = "/{stepId}/delete.json", method = RequestMethod.POST)
	public @ResponseBody Response delete(@PathVariable int stepId) {
		Response response = new Response();
		Integer variableId = calculationStepService.delete(stepId);
		response.addField("deletedVariable", variableId);
		return response;
	}
	
	@RequestMapping(value = "/{stepId}/stepno/{stepNo}.json", method = RequestMethod.POST)
	public @ResponseBody Response updateStepNo(@PathVariable int stepId, @PathVariable int stepNo) {
		Response response = new Response();
		calculationStepService.updateStepNumber(stepId, stepNo);
		return response;
	}
	
	/**
	 * Execute a job for the given calculation step id
	 * @param stepId
	 * @param totalItems
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 */
	@Deprecated
	@RequestMapping(value = "/{stepId}/run.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job executeJob(@PathVariable int stepId, @RequestParam(required=false) Integer totalItems) throws InvalidProcessingChainException, WorkspaceLockedException {
		Workspace workspace = workspaceService.getActiveWorkspace();

		CalculationStep step = calculationStepDao.find(stepId);
		CustomRTask task = (CustomRTask) taskManager.createCalculationStepTask(step);
		if(totalItems != null && totalItems > 0) {
			task.setMaxItems(totalItems);
		}
		
		Job job = taskManager.createJob(workspace);
		job.addTask(task);

		taskManager.startJob(job);

		return job;
	}
	
	/**
	 * Executes a job for the given calculation step id
	 * 
	 * @param stepId
	 * @param parameters Parameters in JSON format
	 * @return
	 * @throws InvalidProcessingChainException
	 * @throws WorkspaceLockedException
	 * @throws ParseException 
	 */
	@RequestMapping(value = "/{stepId}/test.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized Job testCalculationStep(@PathVariable int stepId, @RequestBody String parametersStr) throws InvalidProcessingChainException, WorkspaceLockedException, ParseException {
		Workspace workspace = workspaceService.getActiveWorkspace();

		CalculationStep step = calculationStepDao.find(stepId);
		CalculationStepTestTask task = taskManager.createCalculationStepTestTask(step);
		
		JSONObject parametersJson = (JSONObject) new JSONParser().parse(parametersStr);
		Parameters parameters = CalculationStepTestTask.Parameters.parse(parametersJson);
		task.setParameters(parameters);
		
		Job job = taskManager.createJob(workspace);
		job.addTask(task);
		taskManager.startJob(job);

		return job;
	}

	@RequestMapping(value = "/test/{jobId}/query.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<DataRecord> calculationStepTestQuery(@PathVariable String jobId, @RequestParam int offset, @RequestParam(value = "numberOfRows" , required=false) Integer numberOfRows) {
		CalculationStepTestTask task = getCalculationStepTestTask(jobId);
		if ( task == null ) {
			return Collections.emptyList();
		} else {
			if ( numberOfRows == null ) {
				numberOfRows = 50;
			}
			List<DataRecord> results = task.getResults(offset, offset + numberOfRows);
			return results;
		}
	}
	
	@RequestMapping(value = "/test/{jobId}/count.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody 
	Response calculationStepTestCount(@PathVariable String jobId) {
		Response response = new Response();
		long count = 0;
		CalculationStepTestTask task = getCalculationStepTestTask(jobId);
		if ( task != null ) {
			count = task.getMaxItems();
		}
		response.addField("count", count);
		return response;
	}
	
	private CalculationStepTestTask getCalculationStepTestTask(String jobId) {
		Job activeJob = taskManager.getJobById(jobId);
		List<Task> tasks = activeJob.tasks();
		if ( tasks.size() == 1 && tasks.get(0) instanceof CalculationStepTestTask ) {
			CalculationStepTestTask task = (CalculationStepTestTask) tasks.get(0);
			return task;
		} else {
			return null;
		}
	}

}
