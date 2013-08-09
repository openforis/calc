package org.openforis.calc.chain.post;

import java.util.List;

import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;

/**
 * Copies variable columns as for aggregates (e.g. Min, Max, Median, etc.)
 * with non-null column names (i.e. columns not in original fact table)
 * 
 * @author G. Miceli
 */
public final class CreateAggregateColumnsTask extends Task {
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		List<Entity> entities = workspace.getEntities();
		for (Entity entity : entities) {
			List<Variable> variables = entity.getVariables();
			for (Variable variable : variables) {
//				List<VariableAggregate> aggregates = variable.getVariableAggregates();
				List<VariableAggregate> aggregates = null;
				for (VariableAggregate agg : aggregates) {
//					String columnName 
				}
			}
		}
	}
	private class VariableAggregate {
		
	}
}