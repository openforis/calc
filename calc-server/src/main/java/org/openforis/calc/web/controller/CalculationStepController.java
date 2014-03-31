/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.CalculationStepService;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.module.r.CalcRModule;
import org.openforis.calc.module.r.CustomROperation;
import org.openforis.calc.web.form.CalculationStepForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

	/**
	 * Load all calculation steps for active workspace
	 * @return
	 */
	@RequestMapping(value = "/load.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	List<CalculationStep> loadAll() {
		Workspace workspace = workspaceService.getActiveWorkspace();
		if( workspace != null ) {
			ProcessingChain chain = workspace.getDefaultProcessingChain();
			return calculationStepDao.findByProcessingChain( chain.getId() ) ;
		} else {
			// empty list
			return new ArrayList<CalculationStep>();
		}
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
		response.addField( "deletedStep" , stepId );
		response.addField("deletedVariable", variableId);
		return response;
	}
	
	@RequestMapping(value = "/{stepId}/stepno/{stepNo}.json", method = RequestMethod.POST)
	public @ResponseBody Response updateStepNo(@PathVariable int stepId, @PathVariable int stepNo) {
		Response response = new Response();
		calculationStepService.updateStepNumber(stepId, stepNo);
		return response;
	}
	
}
