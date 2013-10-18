/**
 * 
 */
package org.openforis.calc.schema;

import java.util.HashMap;
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

	private Map<Entity, InputTable> dataTables;
	
	private Map<Entity, EntityDataView> dataViews;
	
	public InputSchema(Workspace workspace) {
		super(workspace.getInputSchema());
		this.workspace = workspace;
		
		initDataTables();
		
		initDataViews();
	}
	
	private void initDataTables() {
		this.dataTables = new HashMap<Entity, InputTable>();
		for ( Entity entity : workspace.getEntities() ) {
			InputTable inputTable = new InputTable(entity, this);
			addTable(inputTable);
			dataTables.put(entity, inputTable);
		}
	}

	private void initDataViews() {
		this.dataViews = new HashMap<Entity, EntityDataView>();
		for ( Entity entity : workspace.getEntities() ) {
			EntityDataView view = new EntityDataView(entity, this);
			addView(view);
			dataViews.put(entity, view);
		}
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public InputTable getDataTable(Entity entity) {
		return dataTables.get(entity);
	}

	public EntityDataView getDataView(Entity entity) {
		return dataViews.get(entity);
	}
}
