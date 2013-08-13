package org.openforis.calc.chain.post;

import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.chain.pre.CreateFactTablesTask;
import org.openforis.calc.engine.SqlTask;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * Creates and populates aggregate tables for sampling unit entities and descendants.
 * One for each AOI level (at AOI/stratum level) is created.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class AggregateSamplingUnitsTask extends SqlTask {
	@Override
	protected void execute() throws Throwable {
		setDefaultSchemaSearchPath();
		Workspace workspace = getWorkspace();

		List<Entity> entities = workspace.getEntities();
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		for (Entity entity : entities) {
			if ( entity.isSamplingUnit() ) {
				for (AoiHierarchy hierarchy : aoiHierarchies) {
					List<AoiHierarchyLevel> levels = hierarchy.getLevels();
					for (AoiHierarchyLevel level : levels) {
						createAggregateTable(entity, level);
					}
				}
			}
		}
	}

	private void createAggregateTable(Entity entity, AoiHierarchyLevel level) {
		String factTable = entity.getDataTable();
		String aoiFkColumn = level.getFkColumn();
		String levelName = level.getName();
		String aggTable = "_agg_"+levelName+"_stratum_"+factTable;
		Integer entityId = entity.getId();
		
		List<String> select = new ArrayList<String>();
		List<String> groupBy = new ArrayList<String>();
		List<Variable> variables = entity.getVariables();
		for (Variable variable : variables) {
			if ( variable instanceof CategoricalVariable ) {
				addDimensionColumn((CategoricalVariable) variable, groupBy);
			} else if ( variable instanceof QuantitativeVariable ){
				addMeasureColumn((QuantitativeVariable) variable, select);
			} else {
				throw new UnsupportedOperationException("Unknown variable class");
			}
		}
		groupBy.add(CreateFactTablesTask.STRATUM_ID);
		groupBy.add(aoiFkColumn);
		select.addAll(0, groupBy);
		
		//add aggregate fact count column
		select.add("count(*) as " + "_agg_cnt");

		if ( isDebugMode() ) {
			psql().dropTableIfExistsCascade(aggTable).execute();
		}
		
		createAggregateTable(factTable, aoiFkColumn, aggTable, entityId, select, groupBy);
	}

	private void addDimensionColumn(CategoricalVariable var, List<String> groupBy) {
		String idCol = var.getCategoryIdColumn();
		if ( idCol != null && var.isDisaggregate() ) {
			groupBy.add(idCol);
		}
	}

	private void addMeasureColumn(QuantitativeVariable var, List<String> select) {
		String valueCol = var.getValueColumn();
		List<VariableAggregate> aggregates = var.getAggregates();
		for (VariableAggregate aggregate : aggregates) {
			String formula = aggregate.getAggregateFormula();
			String aggCol = aggregate.getAggregateColumn();
			aggCol = aggCol == null ? valueCol : aggCol;
			select.add(formula+" as "+aggCol);
		}
	}

	private void createAggregateTable(String factTable, String aoiFkColumn, String aggTable, int entityId, 
			List<String> select, List<String> groupBy) {
		
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