package org.openforis.calc.chain.post;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.chain.pre.CreateFactTablesTask;
import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Creates and populates aggregate tables for all relevant fact tables, creating
 * two tables for each AOI level (at AOI/stratum level and one at AOI level).
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class AggregateFactTablesTask extends SqlTask {
	@Override
	protected void execute() throws Throwable {
		Workspace workspace = getWorkspace();
		setDefaultSchemaSearchPath(workspace);

		List<Entity> entities = workspace.getEntities();
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for (Entity entity : entities) {
			// TODO MAKE DYNAMIC!
			if ( entity.getName().equals("plot") ) {
				for (AoiHierarchy hierarchy : aoiHierarchies) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					for (AoiHierarchyLevel level : levels) {
						aggregateFactTable(entity, level);
					}
				}
			}
		}
	}

	protected void setDefaultSchemaSearchPath(Workspace workspace) {
		psql().setSchemaSearchPath(workspace.getOutputSchema(), Psql.PUBLIC).execute();
	}

	private void aggregateFactTable(Entity entity, AoiHierarchyLevel level) {
		String factTable = entity.getDataTable();
		String aoiFkColumn = level.getFkColumn();
		String levelName = level.getName();
		String aggTable = "_agg_"+levelName+"_stratum_"+factTable;
		Integer entityId = entity.getId();
		
		List<String> select = new ArrayList<String>();
		List<String> groupBy = new ArrayList<String>();
		List<Variable> variables = entity.getVariables();
		for (Variable variable : variables) {
			if (variable instanceof BinaryVariable) {
				// TODO remove workaround once Binary Variables are properly supported (CALC-106)
			} else
			if ( variable instanceof CategoricalVariable ) {
 
				CategoricalVariable catvar = (CategoricalVariable) variable;				
				String idCol = catvar.getCategoryIdColumn();
				if ( idCol != null ) {
					groupBy.add(idCol);
				}
			} else if ( variable instanceof QuantitativeVariable ){
				QuantitativeVariable qvar = (QuantitativeVariable) variable;
				String valueCol = qvar.getValueColumn();
				List<VariableAggregate> aggregates = qvar.getAggregates();
				for (VariableAggregate aggregate : aggregates) {
					String formula = aggregate.getAggregateFormula();
					String aggCol = aggregate.getAggregateColumn();
					aggCol = aggCol == null ? valueCol : aggCol;
					select.add(formula+" as "+aggCol);
				}
			} else {
				throw new UnsupportedOperationException("Unknown variable class");
			}
		}
		groupBy.add(CreateFactTablesTask.STRATUM_ID);
		groupBy.add(aoiFkColumn);
		select.addAll(0, groupBy);

		Psql aggSelect = new Psql()
			.select(select.toArray())
			.from(factTable+" f")
			.innerJoin(CalculateExpansionFactorsTask.EXPF_TABLE+" x")
			 .on("f._stratum_id = x.stratum_id")
			 .and("f."+aoiFkColumn+" = x.aoi_id")
			 .and("x.entity_id = ?")
			 .groupBy(groupBy.toArray());

		psql()
			.createTable(aggTable)
			.as(aggSelect)
			.execute(entityId);
			 
	}
}