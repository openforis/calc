/**
 * 
 */
package org.openforis.calc.rdb;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Table;
import org.openforis.calc.engine.Workspace;

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

	public OutputSchema(Workspace workspace, InputSchema inputSchema) {
		super(workspace.getOutputSchema());
		this.workspace = workspace;
		this.inputSchema = inputSchema;
		this.dataTables = new ArrayList<OutputDataTable>();
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
	}

	public List<OutputDataTable> getDataTables() {
		return dataTables;
	}

}
