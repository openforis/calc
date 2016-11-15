package org.openforis.calc.web.controller;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.AuxiliaryTable;
import org.openforis.calc.metadata.AuxiliaryTableManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Auxiliary tables controller class
 * 
 * @author M. Togna
 *
 */
@Controller
@RequestMapping(value = "/rest/workspace/active/auxiliary-table")
public class AuxiliaryTablesController {

	@Autowired
	private WorkspaceService workspaceService;

	@Autowired
	private AuxiliaryTableManager auxiliaryTableManager;

	@RequestMapping(value = "/save.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody 
	Response save(@RequestParam String schema, @RequestParam String name, @RequestParam(required = false) Long tableId) {
		
		Response response = new Response();

		Workspace workspace = workspaceService.getActiveWorkspace();
		List<AuxiliaryTable> tables = workspace.getAuxiliaryTables();
		for (AuxiliaryTable table : tables) {

			if (!table.getId().equals(tableId) && table.getName().equalsIgnoreCase(name)) {
				response.setStatusError();
				ObjectError objectError = new ObjectError("name", "Auxiliary table name alreay exists");
				response.addError(objectError);
			}

		}

		if (!response.hasErrors()) {
			response.setStatusOk();

			AuxiliaryTable table = new AuxiliaryTable();
			if (tableId != null) {
				table = auxiliaryTableManager.getById(tableId.longValue());
			}
			table.setSchema(schema);
			table.setName(name);
			table.setWorkspace(workspace);

			auxiliaryTableManager.persist(table);

			response.addField("auxiliaryTables", workspace.getAuxiliaryTables());
		}

		return response;
	}

	@RequestMapping(value = "/{id}/delete.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody 
	Response delete(@PathVariable long id) throws IOException {
		
		Response response = new Response();
		Workspace workspace = workspaceService.getActiveWorkspace();
		AuxiliaryTable table = workspace.geAuxiliaryTableById(id);

		auxiliaryTableManager.delete(table);

		response.addField("auxiliaryTables", workspace.getAuxiliaryTables());

		return response;
	}
}
