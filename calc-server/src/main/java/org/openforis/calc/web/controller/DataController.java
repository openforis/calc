/**
 * 
 */
package org.openforis.calc.web.controller;

import java.util.List;

import javax.validation.Valid;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStepDao;
import org.openforis.calc.chain.InvalidProcessingChainException;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.TaskManager;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceLockedException;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.openforis.calc.module.r.CalcRModule;
import org.openforis.calc.module.r.CustomROperation;
import org.openforis.calc.module.r.CustomRTask;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.Schemas;
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
 * @author M. Togna
 * 
 */
@Controller
@RequestMapping(value = "/rest/data")
public class DataController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private VariableDao variableDao;

	@Autowired
	private CalculationStepDao calculationStepDao;

	@Autowired
	private TaskManager taskManager;

	

	@RequestMapping(value = "/{entityName}/load.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	void load(@PathVariable String entityName) {
		Workspace workspace = workspaceService.getActiveWorkspace();
		Entity entity = workspace.getEntityByName(entityName);
		Schemas schemas = new Schemas(workspace);
		InputSchema schema = schemas.getInputSchema();
		EntityDataView dataView = schema.getDataView(entity);
		
		
	}

	

}
