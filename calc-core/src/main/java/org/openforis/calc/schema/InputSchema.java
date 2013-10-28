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

	private Map<Integer, InputTable> dataTables;
	
	private Map<Integer, EntityDataView> dataViews;
	
	public InputSchema(Workspace workspace) {
		super(workspace.getInputSchema());
		this.workspace = workspace;
		
		initDataTables();
		
		initDataViews();
	}
	
	private void initDataTables() {
		this.dataTables = new HashMap<Integer, InputTable>();
		for ( Entity entity : workspace.getEntities() ) {
			InputTable inputTable = new InputTable(entity, this);
			addTable(inputTable);
			dataTables.put(entity.getId(), inputTable);
		}
	}

	private void initDataViews() {
		this.dataViews = new HashMap<Integer, EntityDataView>();
		for ( Entity entity : workspace.getEntities() ) {
			EntityDataView view = new EntityDataView(entity, this);
			addView(view);
			dataViews.put(entity.getId(), view);
		}
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public InputTable getDataTable(Entity entity) {
		return dataTables.get(entity.getId());
	}

	public EntityDataView getDataView(Entity entity) {
		return dataViews.get(entity.getId());
	}
}
