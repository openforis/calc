/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.Variable;
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
	private ProcessingChainManager processingChainManager;
	
	@Autowired
	private TaskManager taskManager;

	// added now for convenience. Mino
	@Autowired( required=true )
	private HttpServletRequest request;
	
	@RequestMapping(value = "/save.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response save(@Valid CalculationStepForm form, BindingResult result) {
		Response response = validate( form, result );
		
		if ( !response.hasErrors() ) {
			Workspace ws = workspaceService.getActiveWorkspace();
			ProcessingChain chain = ws.getDefaultProcessingChain();
			
			CalculationStep step;
			Integer stepId = form.getId();
			if (stepId == null) {
				step = new CalculationStep();
				int stepNo = chain.getCalculationSteps().size() + 1;
				step.setStepNo(stepNo);
				
				chain.addCalculationStep( step );
			} else {
				step = chain.getCalculationStepById( stepId );
			}
			
			Variable<?> outputVariable = ws.getVariableById(form.getVariableId());
			step.setOutputVariable(outputVariable);
			step.setModuleName(CalcRModule.MODULE_NAME);
			step.setModuleVersion(CalcRModule.VERSION_1);
			step.setOperationName(CustomROperation.NAME);
			step.setCaption(form.getCaption());
			
			Type type = CalculationStep.Type.valueOf( request.getParameter("type") );
			step.setType(type);
			
			ParameterMap params = new ParameterHashMap();
			step.setParameters(params);
			
			switch (type) {
			
				case EQUATION:
					Integer listId = form.getEquationList();
					step.setEquationListId( listId.longValue() );
					// populate calc step parameters
					Integer codeVariable = form.getCodeVariable();
					params.setInteger( "codeVariable", codeVariable );
					
					EquationList equationList = ws.getEquationListById(listId);
					Collection<String> equationVariables = equationList.getEquationVariables();
					Map<String, Integer> eqVariablesParam = form.getEquationVariables();
					List<ParameterMap> varParams = new ArrayList<ParameterMap>();
					for ( String equationVariable : equationVariables ){
						
						Integer variableId = eqVariablesParam.get(equationVariable);
					
						ParameterMap varParam = new ParameterHashMap();
						varParam.setString( "equationVariable", equationVariable );
						varParam.setNumber( "variableId", variableId );
						varParams.add( varParam );
					}
					params.setList( "variables", varParams  );
					step.setRScriptFromEquation();
					break;
					
				case SCRIPT:
					step.setScript( form.getScript() );
					break;
			}
				
			processingChainManager.saveCalculationStep( step );

			response.addField( "calculationStep", step );
			response.addField( "processingChain", chain );
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
			return chain.getCalculationSteps();
		} else {
			// empty list
			return new ArrayList<CalculationStep>();
		}
	}

	@RequestMapping(value = "/{stepId}/load.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	CalculationStep load(@PathVariable int stepId) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		ProcessingChain defaultProcessingChain = workspace.getDefaultProcessingChain();
		CalculationStep step = defaultProcessingChain.getCalculationStepById(stepId);
		return step;
	}
	
	@RequestMapping(value = "/{stepId}/delete.json", method = RequestMethod.POST)
	public @ResponseBody Response delete( @PathVariable int stepId ){
		Response response = new Response();
		
		CalculationStep step = load(stepId);
		ProcessingChain processingChain = step.getProcessingChain();

		Integer variableId = processingChainManager.deleteCalculationStep(step);
		
		response.addField( "deletedStep" , stepId );
		response.addField("deletedVariable", variableId);
		response.addField( "processingChain", processingChain );
		return response;
	}
	
	@RequestMapping(value = "/{stepId}/stepno/{stepNo}.json", method = RequestMethod.POST)
	public @ResponseBody Response updateStepNo(@PathVariable int stepId, @PathVariable int stepNo) {
		Response response = new Response();
		CalculationStep step = load(stepId);
		processingChainManager.shiftCalculationStep(step, stepNo);
		return response;
	}
	
}
