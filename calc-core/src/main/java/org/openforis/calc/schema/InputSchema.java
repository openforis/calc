/**
 * 
 */
package org.openforis.calc.schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class InputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;

	private Map<Entity, InputDataTable> dataTables;
	
	public InputSchema(Workspace workspace) {
		super(workspace.getInputSchema());
		this.workspace = workspace;
		initDataTables();
	}
	
	public Workspace getWorkspace() {
		return workspace;
	}
	
	private void initDataTables() {
		this.dataTables = new HashMap<Entity, InputDataTable>();
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			InputDataTable inputTable = new InputDataTable(entity, this);
			addTable(inputTable);
			dataTables.put(entity, inputTable);
		}
	}

	
	public InputDataTable getDataTable(Entity entity) {
		return dataTables.get(entity);
	}

}
