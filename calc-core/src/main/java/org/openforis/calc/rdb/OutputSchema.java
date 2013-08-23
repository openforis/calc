/**
 * 
 */
package org.openforis.calc.rdb;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jooq.Table;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;

/**
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class OutputSchema extends RelationalSchema {

	private static final long serialVersionUID = 1L;

	private Workspace workspace;
	private InputSchema inputSchema;
	private StratumDimensionTable stratumDimensionTable;
	private Map<Entity, OutputDataTable> dataTables;
	private Map<CategoricalVariable, CategoryDimensionTable> categoryDimensionTables;
	private Map<AoiHierarchyLevel, AoiDimensionTable> aoiDimensionTables;

	public OutputSchema(Workspace workspace, InputSchema inputSchema) {
		super(workspace.getOutputSchema());
		this.workspace = workspace;
		this.inputSchema = inputSchema;
		this.stratumDimensionTable = new StratumDimensionTable(this);
		this.dataTables = new HashMap<Entity, OutputDataTable>();
		this.categoryDimensionTables = new HashMap<CategoricalVariable, CategoryDimensionTable>();
		this.aoiDimensionTables = new HashMap<AoiHierarchyLevel, AoiDimensionTable>();
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
			OutputDataTable dataTable = (OutputDataTable) table;
			Entity entity = dataTable.getEntity();
			dataTables.put(entity, dataTable);
		}
		if ( table instanceof CategoryDimensionTable ) {
			CategoryDimensionTable dimTable = (CategoryDimensionTable) table;
			categoryDimensionTables.put(dimTable.getVariable(), dimTable);
		}
		if ( table instanceof AoiDimensionTable ) {
			AoiDimensionTable aoiDimTable = (AoiDimensionTable) table;
			aoiDimensionTables.put(aoiDimTable.getHierarchyLevel(), aoiDimTable);
		}
	}

	public StratumDimensionTable getStratumDimensionTable() {
		return stratumDimensionTable;
	}

	public Collection<OutputDataTable> getDataTables() {
		return Collections.unmodifiableCollection(dataTables.values());
	}

	public Collection<CategoryDimensionTable> getCategoryDimensionTables() {
		return Collections.unmodifiableCollection(categoryDimensionTables.values());
	}

	public AoiDimensionTable getAoiDimensionTable(AoiHierarchyLevel aoiHierarchyLevel) {
		return aoiDimensionTables.get(aoiHierarchyLevel);
	}

	public CategoryDimensionTable getCategoryDimensionTable(CategoricalVariable variable) {
		return categoryDimensionTables.get(variable);
	}
}
