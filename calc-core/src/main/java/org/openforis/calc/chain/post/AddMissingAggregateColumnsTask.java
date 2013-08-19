package org.openforis.calc.chain.post;

import static org.openforis.calc.persistence.postgis.Psql.FLOAT8;
import static org.openforis.calc.persistence.postgis.Psql.table;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;

/**
 * Adds missing aggregates columns to fact table
 * 
 * @author G. Miceli
 */
public final class AddMissingAggregateColumnsTask extends Task {
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		String outputSchema = workspace.getOutputSchema();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			String factTable = table(outputSchema, entity.getDataTable());
			List<Variable> variables = entity.getVariables();
			for (Variable variable : variables) {
				if ( variable instanceof QuantitativeVariable ) {
					QuantitativeVariable qvar = (QuantitativeVariable) variable;
					List<VariableAggregate> aggregates = qvar.getAggregates();
					String valueColumn = qvar.getValueColumn();
					for (VariableAggregate agg : aggregates) {
						String aggColumn = agg.getAggregateColumn();
						if ( aggColumn != null && !valueColumn.equals(aggColumn) ) {
							addAggregateColumn(factTable, aggColumn);
							updateAggregateColumn(factTable, valueColumn, aggColumn);
						}
					}
				}
			}
		}
	}

	private void updateAggregateColumn(String factTable, String valueColumn,
			String aggColumn) {
		psql()
			.update(factTable)
			.set(aggColumn + "=" + valueColumn)
			.execute();
	}
	
	private void addAggregateColumn(String factTable, String aggColumn) {
		psql()
			.alterTable(factTable)
			.addColumn(aggColumn, FLOAT8)
			.execute();
	}
}