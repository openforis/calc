package org.openforis.calc.chain.post;

import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.AggregateTable;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.OutputSchema;

/**
 * Creates and populates aggregate tables for sampling unit entities and descendants.
 * One for each AOI level (at AOI/stratum level) is created.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CreateAggregateTablesTask extends Task {
	@Override
	protected void execute() throws Throwable {
		// TODO agg_count and threshold
		OutputSchema outputSchema = getOutputSchema();
		List<AggregateTable> aggTables = outputSchema.getAggregateTables();
		ExpansionFactorTable expf = outputSchema.getExpansionFactorTable();
		
		for (AggregateTable aggTable : aggTables) {
			
			AoiHierarchyLevel level = aggTable.getAoiHierarchyLevel();
			FactTable f = (FactTable) aggTable.getSourceTable();
			Field<Integer> aoiId = f.getAoiIdField(level);
			Field<Integer> stratumId = f.getStratumIdField();
			Entity entity = aggTable.getEntity();
			Integer entityId = entity.getId();
			
			SelectQuery<?> select = new Psql().selectQuery(f);
			select.addSelect(f.getCategoryValueFields());
			select.addSelect(f.getDimensionIdFields());
			select.addSelect(stratumId);
			
			// Select AOI ID columns
			Collection<Field<Integer>> aoiIdFields = aggTable.getAoiIdFields();
			for (Field<Integer> aoiIdField : aoiIdFields) {
				select.addSelect(f.field(aoiIdField));
			}
			
			select.addGroupBy( select.getSelect() );
			select.addGroupBy( expf.EXPF );
			
			// Add aggregate columns
			List<VariableAggregate> variableAggregates = entity.getVariableAggregates();
			for (VariableAggregate varAgg : variableAggregates) {
				String formula = varAgg.getAggregateFormula();
				String aggCol = varAgg.getAggregateColumn();
				select.addSelect(DSL.field(formula).as(aggCol));
			}
			//add aggregate fact count column
			select.addSelect(DSL.count().as(aggTable.getAggregateFactCountField().getName()));
			
			if ( isDebugMode() ) {
				psql()
					.dropTableIfExists(aggTable)
					.execute();
			}
			
			select.addJoin(expf, 
					stratumId.eq(expf.STRATUM_ID)
					.and(aoiId.eq(expf.AOI_ID))
					.and(expf.ENTITY_ID.eq(entityId))
					);
			
			psql()
				.createTable(aggTable)
				.as(select)
				.execute();
			
			// Grant access to system user
			psql()
				.grant(Privilege.ALL)
				.on(aggTable)
				.to(getSystemUser())
				.execute();
			
//			String aoiFkColumn = level.getFkColumn();
//			String levelName = level.getName();
//			String aggTable = "_agg_"+levelName+"_stratum_"+factTable;
//			Integer entityId = entity.getId();
//			
//			List<String> select = new ArrayList<String>();
//			List<String> groupBy = new ArrayList<String>();
//			List<Variable> variables = entity.getVariables();
//			for (Variable variable : variables) {
//				if ( variable instanceof CategoricalVariable ) {
//					addDimensionColumn((CategoricalVariable) variable, groupBy);
//				} else if ( variable instanceof QuantitativeVariable ){
//					addMeasureColumn((QuantitativeVariable) variable, select);
//				} else {
//					throw new UnsupportedOperationException("Unknown variable class");
//				}
//			}
//			groupBy.add(STRATUM_ID);
//			groupBy.add(aoiFkColumn);
//			select.addAll(0, groupBy);
//			
//			//add aggregate fact count column
//			select.add("count(*) as " + "_agg_cnt");
//	
//			if ( isDebugMode() ) {
//				createPsqlBuilder().dropTableIfExistsCascade(aggTable).execute();
//			}
//			
//			createAggregateTable(factTable, aoiFkColumn, aggTable, entityId, select, groupBy);
		}
	}
	
//	private void addDimensionColumn(CategoricalVariable var, List<String> groupBy) {
//		String idCol = var.getCategoryIdColumn();
//		if ( idCol != null && var.isDisaggregate() ) {
//			groupBy.add(idCol);
//		}
//	}
//
//	private void addMeasureColumn(QuantitativeVariable var, List<String> select) {
//		String valueCol = var.getValueColumn();
//		List<VariableAggregate> aggregates = var.getAggregates();
//		for (VariableAggregate aggregate : aggregates) {
//			String formula = aggregate.getAggregateFormula();
//			String aggCol = aggregate.getAggregateColumn();
//			aggCol = aggCol == null ? valueCol : aggCol;
//			select.add(formula+" as "+aggCol);
//		}
//	}
//
//	private void createAggregateTable(String factTable, String aoiFkColumn, String aggTable, int entityId, 
//			List<String> select, List<String> groupBy) {
//		
//		PsqlBuilder aggSelect = new PsqlBuilder()
//				.select(select.toArray())
//				.from(factTable+" f")
//				.innerJoin(CalculateExpansionFactorsTask.EXPF_TABLE+" x")
//				.on("f._stratum_id = x.stratum_id")
//				.and("f."+aoiFkColumn+" = x.aoi_id")
//				.and("x.entity_id = ?")
//				.groupBy(groupBy.toArray());
//		
//		createPsqlBuilder()
//			.createTable(aggTable)
//			.as(aggSelect)
//			.execute(entityId);
//	}
}