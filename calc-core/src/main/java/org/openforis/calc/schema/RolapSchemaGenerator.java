package org.openforis.calc.schema;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
@Component
public class RolapSchemaGenerator {
	
	public RolapSchema createRolapSchema(Workspace workspace, OutputSchema outputSchema) {
		RolapSchema rolapSchema = new RolapSchema(workspace, outputSchema);

		addCubes(rolapSchema);
		
		// TODO other stuff
		
		return rolapSchema;
	}


	private void addCubes(RolapSchema rolapSchema) {
		Workspace workspace = rolapSchema.getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			if ( entity.isUnitOfAnalysis() ) {
				addCube(rolapSchema, entity);
			}
		}
	}

	private void addCube(RolapSchema rolapSchema, Entity entity) {
		Cube cube = new Cube(rolapSchema);
		List<Variable> variables = entity.getVariables();
		for (Variable var : variables) {
			if (var instanceof CategoricalVariable ) {
				addCategoryDimension(cube, (CategoricalVariable) var);
			} else if (var instanceof QuantitativeVariable) {
				addVariableMeasures(cube, (QuantitativeVariable) var);
			}
		}
		rolapSchema.addCube(cube);
	}


	private void addCategoryDimension(Cube cube, CategoricalVariable variable) {
		RolapSchema rolapSchema = cube.getRolapSchema();
		if ( variable.isDisaggregate() ) {
			if ( variable.isDegenerateDimension() ) {
				// TODO support for degenerate dimensions
			} else {
				// Relational database table
//				RelationalSchema relationalSchema = rolapSchema.getOutputSchema();
//				CategoryDimensionTable table = new CategoryDimensionTable(relationalSchema, variable);
//				relationalSchema.addTable(table);
				
				// OLAP dimension
//				CategoryDimension dim = new CategoryDimension(variable, table);
//				rolapSchema.addCategoryDimension(dim);
//				cube.addDimensionUsage(dim);
				// TODO
			}
		}
	}
	
	private void addVariableMeasures(Cube cube, QuantitativeVariable var) {
		List<VariableAggregate> aggs = var.getAggregates();
		for (VariableAggregate agg : aggs) {
			addVariableMeasure(cube, agg);
		}
	}

	private void addVariableMeasure(Cube cube, VariableAggregate agg) {
		Measure measure = new Measure(cube, agg);
		cube.addMeasure(measure);
	}
}
