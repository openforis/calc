/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.chain.CalculationStepRScriptGenerator;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.chain.ProcessingChainManager;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Category;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.ParameterMapConverter;
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
	
	private static final String MODULE_NAME 		= "calc-r";
	private static final String MODULE_VERSION_2 	= "2.0";
	private static final String OPERATION_NAME 		= "exec-r";

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private ProcessingChainManager processingChainManager;
	
//	@Autowired
//	private TaskManager taskManager;

	@Autowired
	private CalculationStepRScriptGenerator calculationStepRScriptGenerator;
	
//	@Autowired
//	private CategoryManager categoryManager;
	
	@RequestMapping(value = "/save.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Response save(@Valid CalculationStepForm form, BindingResult result) {
		Response response = validate( form, result );
		
		if ( !response.hasErrors() ) {
			Workspace ws = workspaceService.getActiveWorkspace();
			ProcessingChain chain = ws.getDefaultProcessingChain();
			
			CalculationStep step = convertFormToCalculationStep(form, chain);
			
			// set parameters place holder
			ParameterMap params = new ParameterHashMap();
			step.setParameters(params);
			
			// set output variable
			Variable<?> outputVariable = ws.getVariableById( form.getVariableId() );
			step.setOutputVariable( outputVariable );
			
			String aggregateParameters = "{}";
			switch ( step.getType() ) {
			
				case EQUATION:
					populateStepTypeEquation( form, ws, step, params );
				case SCRIPT:
					step.setScript( form.getScript() );
					aggregateParameters = form.getAggregateParameters();
					break;
				case CATEGORY:
					populateStepTypeCategory( form, ws, step, params );
//					boolean varCreated = populateStepTypeCategory( form, ws, step, params );
//					if( varCreated ) {
//						response.addField( "addedVariable", step.getOutputVariable() );
//					}
					break;
			}
			// set r script
			String rScript = calculationStepRScriptGenerator.generateRScript( step );
			step.setScript(rScript);
			// set aggregate parameters
			step.setAggregateParameters( new ParameterMapConverter().from(aggregateParameters) );
			
			// save calc step
			processingChainManager.saveCalculationStep( step );

			response.addField( "calculationStep", step );
			response.addField( "processingChain", chain );
		}
		return response;
	}

	// returns true if a new output variable has been created
	protected void populateStepTypeCategory(CalculationStepForm form, Workspace ws, CalculationStep step, ParameterMap params) {
//		boolean varCreated = false;
		
		Integer categoryId = form.getCategoryId();
		params.setInteger( "categoryId", categoryId );

		Category category = ws.getCategoryById(categoryId);
		CategoryLevel defualtLevel = category.getHierarchies().get(0).getLevels().get(0);
		
		Variable<?> variable = step.getOutputVariable();
//		
//		if( variable == null || variable instanceof QuantitativeVariable) {
//			Entity entity = ws.getEntityById( form.getEntityId() );
//			String name = StringUtils.normalize( step.getCaption() ) ;
//			variable = workspaceService.addMultiwayVariable( entity , name );
//			step.setOutputVariable( variable );
//			varCreated = true;
//		}
//		
//		// set level to variable
		( (MultiwayVariable) variable ).setCategoryLevel( defualtLevel );
		workspaceService.saveVariable( variable );
		
		step.setScript( form.getScript() );
		// prepare params
		// only r script at the moment
//		JSONArray categoryClasses = categoryManager.loadCategoryClasses( ws, categoryId );
//		List<ParameterMap> classParams = new ArrayList<ParameterMap>();
//		
//		for (Object object : categoryClasses) {
//			JSONObject categoryClass = (JSONObject) object;
//			
//			int classId = Integer.parseInt( categoryClass.get( "id" ).toString() );
//			if( classId != -1 ){
//				String code = form.getCategoryClassCodes().get( classId );
//				Integer variableId = form.getCategoryClassVariables().get( classId );
//				
//				String condition = form.getCategoryClassConditions().get( classId );
//				
//				CalculationStepCategoryClassParameters classParam = new CalculationStepCategoryClassParameters();
//				
//				classParam.setClassId( classId );
//				classParam.setClassCode( code );
//				classParam.setVariableId( variableId );
//				classParam.setCondition( condition );
//				
//				String left = form.getCategoryClassLeftConditions().get( classId );
//				String right = form.getCategoryClassRightConditions().get( classId );
//				
//				if( !(condition.equals("IS NULL") || condition.equals("IS NOT NULL")) ) {
//					classParam.setLeft( left );
//					if( condition.equals("BETWEEN") || condition.equals("NOT BETWEEN") ){
//						classParam.setRight( right );
//					}
//				}
//				
//				classParams.add( classParam );
//			}
//		}
//		params.setList( "categoryClassParameters", classParams );
		
//		return varCreated;
	}

	protected void populateStepTypeEquation(CalculationStepForm form, Workspace ws, CalculationStep step, ParameterMap params) {
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
	}

	private CalculationStep convertFormToCalculationStep(CalculationStepForm form, ProcessingChain chain) {
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
		
		step.setModuleName( MODULE_NAME );
		step.setModuleVersion( MODULE_VERSION_2 );
		step.setOperationName( OPERATION_NAME );
		step.setCaption( form.getCaption() );
		step.setActive( form.getActive() );
		
		Type type = CalculationStep.Type.valueOf( form.getType() );
		step.setType(type);
		
		return step;
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
		response.addField( "deletedVariableId", variableId );
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
	
	@RequestMapping(value = "/{stepId}/active/{active}.json", method = RequestMethod.POST)
	public @ResponseBody Response updateActive(@PathVariable int stepId, @PathVariable Boolean active) {
		
		CalculationStep step = load( stepId );
		step.setActive(active);
		processingChainManager.saveCalculationStep( step , false );
		
		Response response	 = new Response();
		response.addField( "calculationStep" , step );
		return response;
	}
	
	
}
