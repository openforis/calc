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
 * Creates and populates aggregate tables for sampling unit entities and descendants. One for each AOI level (at AOI/stratum level) is created.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CreateAggregateTablesTask extends Task {
	@Override
	protected void execute() throws Throwable {
		// TODO threshold
		OutputSchema outputSchema = getOutputSchema();
		Collection<AggregateTable> aggTables = outputSchema.getAggregateTables();
		ExpansionFactorTable expf = outputSchema.getExpansionFactorTable();
		for (AggregateTable aggTable : aggTables) {
			AoiHierarchyLevel level = aggTable.getAoiHierarchyLevel();
			FactTable f = (FactTable) aggTable.getSourceFactTable();
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
				

			select.addJoin(expf, stratumId.eq(expf.STRATUM_ID)
				  .and(aoiId.eq(expf.AOI_ID))
				  .and(expf.ENTITY_ID.eq(entityId)));

			psql().createTable(aggTable).as(select).execute();

			// Grant access to system user
			psql()
				.grant(Privilege.ALL)
				.on(aggTable)
				.to(getSystemUser())
				.execute();
				
			}
		}
	}
}
