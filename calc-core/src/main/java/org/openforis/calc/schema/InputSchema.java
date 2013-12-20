/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
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
// TODO rename InputSchema to Schema? or DataSchema?
public class InputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;

	private Map<Integer, InputTable> dataTables;
//	private Map<Integer, ResultTable> resultTables;
	
	private Map<Integer, EntityDataView> dataViews;
	
	public InputSchema(Workspace workspace) {
		super(workspace.getInputSchema());
		this.workspace = workspace;
		
		initDataTables();
//		initResultTables();
		
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

//	private void initResultTables() {
//		this.resultTables = new HashMap<Integer, ResultTable>();
//		for ( Entity entity : workspace.getEntities() ) {
//			ResultTable table = new ResultTable(entity, this);
//			addTable(table);
//			resultTables.put(entity.getId(), table);
//		}
//	}
	
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
	
	public ResultTable getResultTable(Entity entity) {
		return this.getResultTable(entity, false);
	}

	public ResultTable getResultTable(Entity entity, boolean temporary) {
		if( entity.getOutputVariables().size() > 0 ){
			ResultTable table = new ResultTable(entity, this, temporary);
			return table;
		}
		return null;
	}
	
	public EntityDataView getDataView(Entity entity) {
		return dataViews.get(entity.getId());
	}
	
	public List<NewFactTable> getFactTables() {
		List<NewFactTable> tables = new ArrayList<NewFactTable>();
		for ( Entity entity : workspace.getEntities() ) {
			if( entity.isAggregable() ) {
				NewFactTable factTable = new NewFactTable(entity, this, getDataView(entity), null);
				tables.add(factTable);
			}
		}
		return tables;
	}
	
}
