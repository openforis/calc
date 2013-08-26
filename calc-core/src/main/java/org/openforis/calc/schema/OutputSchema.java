/**
 * 
 */
package org.openforis.calc.schema;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;

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
		initCategoryDimensionTables();
		initStratumDimensionTable();
		initAoiDimensionTables();
		initDataTables();
		initFactTables();
	}

	private void initCategoryDimensionTables() {
		this.categoryDimensionTables = new HashMap<CategoricalVariable, CategoryDimensionTable>();
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			// Add dimensions for categorical variables
			List<Variable> variables = entity.getVariables();
			for ( Variable var : variables ) {
				if ( var instanceof CategoricalVariable ) {
					CategoryDimensionTable table = new CategoryDimensionTable(this, (CategoricalVariable) var);
					addTable(table);
					categoryDimensionTables.put((CategoricalVariable) var, table);
				}
			}
		}
	}

	private void initFactTables() {
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			if ( entity.isUnitOfAnalysis() ) {
				FactTable table = new FactTable(entity, this);
				addTable(table);
			}
		}		
	}


	private void initStratumDimensionTable() {
		this.stratumDimensionTable = new StratumDimensionTable(this);
	}

	private void initDataTables() {
		this.dataTables = new HashMap<Entity, OutputDataTable>();
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			InputDataTable inputTable = inputSchema.getDataTable(entity);
			OutputDataTable outputTable = new OutputDataTable(entity, this, inputTable);
			addTable(outputTable);
			dataTables.put(entity, outputTable);
		}
	}

	private void initAoiDimensionTables() {
		this.aoiDimensionTables = new HashMap<AoiHierarchyLevel, AoiDimensionTable>();
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			List<AoiHierarchyLevel> levels = aoiHierarchy.getLevels();
			for ( AoiHierarchyLevel level : levels ) {
				AoiDimensionTable table = new AoiDimensionTable(this, level);
				addTable(table);
				aoiDimensionTables.put(level, table);
			}
		}
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public InputSchema getInputSchema() {
		return inputSchema;
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

	public Collection<AoiDimensionTable> getAoiDimensionTables() {
		return Collections.unmodifiableCollection(aoiDimensionTables.values());
	}
	
	public AoiDimensionTable getAoiDimensionTable(AoiHierarchyLevel aoiHierarchyLevel) {
		return aoiDimensionTables.get(aoiHierarchyLevel);
	}

	public CategoryDimensionTable getCategoryDimensionTable(CategoricalVariable variable) {
		return categoryDimensionTables.get(variable);
	}
}
