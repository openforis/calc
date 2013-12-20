package org.openforis.calc.chain.post;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.DSL;
import org.openforis.calc.engine.CalcJob;
import org.openforis.calc.engine.Job;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.AggregateTable;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.NewFactTable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.PlotAggregateTable;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;

/**
 * Creates and populates aggregate tables for sampling unit entities and descendants. One for each AOI level (at AOI/stratum level) is created.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class CreateAggregateTablesTask extends Task {
	
//	private Entity entity;

	public CreateAggregateTablesTask() {
		super();
//		this.entity = entity;
	}

	@Override
	protected long countTotalItems() {
		return getJob().getInputSchema().getFactTables().size();
	}
	
	protected void execute() throws Throwable {
		CalcJob job = (CalcJob) getJob();
	
		Schemas schemas = job.getSchemas();
		InputSchema schema = schemas.getInputSchema();
		
		List<NewFactTable> factTables = schema.getFactTables();
		for (NewFactTable factTable : factTables) {
			Entity entity = factTable.getEntity();
			if( entity.getParent().isSamplingUnit() ){
				PlotAggregateTable plotAgg = factTable.getPlotAggregateTable();
				
				SelectQuery<Record> select = psql().selectQuery();
				select.addFrom( factTable );
				
				select.addSelect( factTable.getParentIdField() );
				select.addGroupBy( factTable.getParentIdField() );
				select.addSelect( factTable.getDimensionIdFields() );
				select.addGroupBy( factTable.getDimensionIdFields() );
				
				// for now quantity fields. check if it needs to be done for each variable aggregate
				for (QuantitativeVariable var : entity.getOutputVariables()) {
					Field<BigDecimal> quantityField = factTable.getQuantityField(var);
					
					select.addSelect( DSL.sum( quantityField.div(factTable.getPlotAreaField()) ).as(quantityField.getName()) );
				}
				
				// drop table
				psql()
					.dropTableIfExists( plotAgg )
					.execute();
				
				int execute = psql()
					.createTable(plotAgg)
					.as(select)
					.execute();
				
			}
			
		}
//		for (Entity entity : getWorkspace().getEntities()) {
//			ResultTable resultTable = schema.getResultTable(entity);
//			if( resultTable != null ){
//				// aggregate based on the sampling design
//				if( entity.getParent().isSamplingUnit() ){
//					resultTable.getPlotAggregateTable();
//				} else {
//					// nothing for now
//				}
//			}
//		}
		
//		job.get
	}	
	
	@Override
	public String getName() {
		return String.format( "Aggregate Tables" );
	}
	
//	@Override
	protected void oldExecute() throws Throwable {
		// TODO threshold
		OutputSchema outputSchema = getOutputSchema();
		Collection<AggregateTable> aggTables = outputSchema.getAggregateTables();
		ExpansionFactorTable expf = outputSchema.getExpansionFactorTable();
		for (AggregateTable aggTable : aggTables) {
			AoiLevel level = aggTable.getAoiHierarchyLevel();
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
				if( !varAgg.isVirtual() ){
					String formula = varAgg.getAggregateFormula();
					String aggCol = varAgg.getAggregateColumn();
					select.addSelect(DSL.field(formula).as(aggCol));
				}
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
