package org.openforis.calc.rolap;

import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
@Component
public class RolapSchemaFactory {
	public RolapSchema createRolapSchema(Workspace workspace) {
		RolapSchema rolapSchema = new RolapSchema(workspace);
		createCategoricalVariableDimensionTables(rolapSchema);
		// TODO
		return rolapSchema;
	}

	private void createCategoricalVariableDimensionTables(RolapSchema rolapSchema) {
		Workspace workspace = rolapSchema.getWorkspace();
		RelationalSchema relationalSchema = rolapSchema.getRelationalSchema();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();

			for (Variable var : variables) {
				if (var instanceof CategoricalVariable ) {
					CategoricalVariable variable = (CategoricalVariable) var;
					if ( variable.isDisaggregate() ) {
						if ( variable.isDegenerateDimension() ) {
							// TODO support for degenerate dimensions
						} else {
							// Relational database table
							CategoryDimensionTable table = new CategoryDimensionTable(relationalSchema, variable);
							relationalSchema.addTable(table);
							// OLAP dimension
							CategoricalVariableDimension dim = new CategoricalVariableDimension(variable, table);
							rolapSchema.addSharedDimension(dim);
						}
					}					
				}
			}
		}
		
		// TODO Auto-generated method stub
		
	}
}
