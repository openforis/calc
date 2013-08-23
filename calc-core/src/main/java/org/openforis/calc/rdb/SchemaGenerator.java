package org.openforis.calc.rdb;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
@Component
public class SchemaGenerator {

	public OutputSchema createOutputSchema(Workspace workspace) {
		InputSchema in = new InputSchema(workspace);
		OutputSchema out = new OutputSchema(workspace, in);
		addCategoryDimensionTables(out);
		addDataTables(out);
		addFactTables(out);
		// TODO
		return out;
	}

	private void addCategoryDimensionTables(OutputSchema out) {
		Workspace workspace = out.getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			// Add variable columns
			List<Variable> variables = entity.getVariables();
			for (Variable var : variables) {
				if ( var instanceof CategoricalVariable ) {
					CategoryDimensionTable table = new CategoryDimensionTable(out, (CategoricalVariable) var);
					out.addTable(table);
				}
			}
		}
	}

	private void addDataTables(OutputSchema out) {
		Workspace workspace = out.getWorkspace();
		List<Entity> entities = workspace.getEntities();
		InputSchema in = out.getInputSchema();
		for ( Entity entity : entities ) {
			InputDataTable inputTable = new InputDataTable(entity, in);
			DataTable outputTable = new OutputDataTable(entity, out, inputTable);
			in.addTable(inputTable);
			out.addTable(outputTable);
		}
	}

	private void addFactTables(OutputSchema out) {
		Workspace workspace = out.getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			if ( entity.isUnitOfAnalysis() ) {
				List<Variable> variables = entity.getVariables();
				for (Variable var : variables) {
					if (var instanceof CategoricalVariable ) {
//						addCategoryDimension(cube, (CategoricalVariable) var);
					} else if (var instanceof QuantitativeVariable) {
//						addVariableMeasures(cube, (QuantitativeVariable) var);
					}
				}
			}
		}		
	}
}
