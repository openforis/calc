/**
 * 
 */
package org.openforis.calc.rdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Table;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class OutputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;
	private InputSchema inputSchema;
	private List<OutputDataTable> dataTables;
	private StratumDimensionTable stratumDimensionTable;
	private Map<CategoricalVariable, CategoryDimensionTable> categoryDimensionTables;
	
	public OutputSchema(Workspace workspace, InputSchema inputSchema) {
		super(workspace.getOutputSchema());
		this.workspace = workspace;
		this.inputSchema = inputSchema;
		this.dataTables = new ArrayList<OutputDataTable>();
		this.stratumDimensionTable = new StratumDimensionTable(this);
		this.categoryDimensionTables = new HashMap<CategoricalVariable, CategoryDimensionTable>();
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public InputSchema getInputSchema() {
		return inputSchema;
	}

	@Override
	public void addTable(Table<?> table) {
		super.addTable(table);
		if ( table instanceof OutputDataTable ) {
			dataTables.add((OutputDataTable) table);
		}
		if ( table instanceof CategoryDimensionTable ) {
			categoryDimensionTables.put(((CategoryDimensionTable) table).getVariable(), (CategoryDimensionTable) table);
		}
	}

	public List<OutputDataTable> getDataTables() {
		return dataTables;
	}

	public StratumDimensionTable getStratumDimensionTable() {
		return stratumDimensionTable;
	}
	
	public List<CategoryDimensionTable> getCategoryDimensionTables() {
		return new ArrayList<CategoryDimensionTable>(categoryDimensionTables.values());
	}
	
	public CategoryDimensionTable getCategoryDimensionTable(CategoricalVariable variable) {
		return categoryDimensionTables.get(variable);
	}
}
