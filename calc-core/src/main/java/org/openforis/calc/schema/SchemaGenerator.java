package org.openforis.calc.schema;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
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

	public Schemas createSchemas(Workspace workspace) {
		Schemas schemas = new Schemas(workspace);
		addCategoryDimensionTables(schemas);
		addAoiDimensionTables(schemas);
		addDataTables(schemas);
		addFactTables(schemas);
		// TODO
		return schemas;
	}

	private void addDataTables(Schemas schemas) {
		Workspace workspace = schemas.getWorkspace();
		List<Entity> entities = workspace.getEntities();
		InputSchema in = schemas.getInputSchema();
		OutputSchema out = schemas.getOutputSchema();
		for ( Entity entity : entities ) {
			InputDataTable inputTable = new InputDataTable(entity, in);
			DataTable outputTable = new OutputDataTable(entity, out, inputTable);
			in.addTable(inputTable);
			out.addTable(outputTable);
		}
	}

	private void addCategoryDimensionTables(Schemas schemas) {
		Workspace workspace = schemas.getWorkspace();
		OutputSchema out = schemas.getOutputSchema();
		List<Entity> entities = workspace.getEntities();
		for ( Entity entity : entities ) {
			// Add variable columns
			List<Variable> variables = entity.getVariables();
			for ( Variable var : variables ) {
				if ( var instanceof CategoricalVariable ) {
					CategoryDimensionTable table = new CategoryDimensionTable(out, (CategoricalVariable) var);
					out.addTable(table);
				}
			}
		}
	}

	private void addAoiDimensionTables(Schemas schemas) {
		Workspace workspace = schemas.getWorkspace();
		OutputSchema out = schemas.getOutputSchema();
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for ( AoiHierarchy aoiHierarchy : aoiHierarchies ) {
			List<AoiHierarchyLevel> levels = aoiHierarchy.getLevels();
			for ( AoiHierarchyLevel aoiHierarchyLevel : levels ) {
				AoiDimensionTable table = new AoiDimensionTable(out, aoiHierarchyLevel);
				out.addTable(table);
			}
		}
	}

	private void addFactTables(Schemas schemas) {
		Workspace workspace = schemas.getWorkspace();
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
