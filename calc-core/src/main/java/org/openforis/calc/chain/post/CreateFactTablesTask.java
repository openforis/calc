package org.openforis.calc.chain.post;

import java.awt.ItemSelectable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Task;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.CreateTableStep.AsStep;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.NewFactTable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.OutputTable;

/**
 * Creates and populates fact tables for entities marked "unit of analysis"
 * 
 * @author Mino Togna
 */
public final class CreateFactTablesTask extends Task {
	
	@Override
	public String getName() {
		return "Create data tables for aggregations";
	}
	
	protected void execute() throws Throwable {
		InputSchema schema = getInputSchema();
		List<NewFactTable> factTables = schema.getFactTables();
		
		for (NewFactTable factTable : factTables) {
			EntityDataView dataTable = factTable.getEntityView();
			
			SelectQuery<?> select = new Psql().selectQuery(dataTable);
			select.addSelect(dataTable.getIdField());
//			select.addSelect(dataTable.getAoiIdFields());
			for (Field<Integer> field : factTable.getDimensionIdFields()) {
				// todo add dim fields to entitydataview
				select.addSelect( dataTable.field(field) );
			}
			
			// select measure
			List<QuantitativeVariable> vars = factTable.getEntity().getQuantitativeVariables();
			for (QuantitativeVariable var : vars) {
				Field<BigDecimal> fld = dataTable.getQuantityField(var);
				select.addSelect(fld);
				
//				for (VariableAggregate agg : var.getAggregates()) {
//						Field<BigDecimal> measureFld = factTable.getVariableAggregateField(agg);
//						Field<BigDecimal> valueFld = dataTable.getQuantityField(var);
//						select.addSelect( valueFld.as(measureFld.getName()) );
////					}
//				}
			}
			
			// add plot area
			TableField<Record,BigDecimal> plotAreaField = factTable.getPlotAreaField();
			if(plotAreaField != null) {
//				select.addSelect( dataTable.field(plotAreaField) );
			}
			
			psql()
			.dropTableIfExists(factTable)
			.execute();
			
			AsStep as = psql()
			.createTable(factTable)
			.as(select);
			
			as.execute();
		
		// Grant access to system user
		psql()
			.grant(Privilege.ALL)
			.on(factTable)
			.to(getSystemUser())
			.execute();
		
			incrementItemsProcessed();
		}
		
	}
	
	@Override
	protected long countTotalItems() {
		return getInputSchema().getFactTables().size();
	}
	
//	@Override
	protected void old_execute() throws Throwable {
		OutputSchema outputSchema = getOutputSchema();
		Collection<FactTable> factTables = outputSchema.getFactTables();
		
		for (FactTable factTable : factTables) {
			OutputTable outputTable = (OutputTable) factTable.getSourceOutputTable();
			
			SelectQuery<?> select = new Psql().selectQuery(outputTable);
			select.addSelect(outputTable.getIdField());
			select.addSelect(outputTable.getAoiIdFields());
			
			selectDimensionsRecursive(select, factTable, outputTable);			
			selectQuantities(select, outputTable);
			selectMeasures(select, factTable);
			
			Field<Integer> stratumId = factTable.getStratumIdField();
			select.addSelect(Psql.nullAs(stratumId));
			
			if ( factTable.getEntity().isSamplingUnit() ) {
				addStratumId(select, factTable);
			}
			
			if ( isDebugMode() ) {
				psql()
					.dropTableIfExists(factTable)
					.execute();
			}
			
			psql()
				.createTable(factTable)
				.as(select)
				.execute();
			
			// Grant access to system user
			psql()
				.grant(Privilege.ALL)
				.on(factTable)
				.to(getSystemUser())
				.execute();
		}
	}

	private void addStratumId(SelectQuery<?> select, FactTable factTable) {
//		OutputSchema outputSchema = (OutputSchema) factTable.getSchema();
//		select.addSelect(SAMPLING_UNIT.STRATUM_ID.as(factTable.getStratumIdField().getName()));
//		Condition cond = SAMPLING_UNIT.CLUSTER.eq(DSL.field(""))
//				.and();
//		select.addJoin(SAMPLING_UNIT, JoinType.LEFT_OUTER_JOIN, cond);
		
	}

	private void selectQuantities(SelectQuery<?> select, OutputTable outputTable) {
		Entity entity = outputTable.getEntity();
		List<QuantitativeVariable> vars = entity.getQuantitativeVariables();
		for (QuantitativeVariable var : vars) {
			Field<BigDecimal> fld = outputTable.getQuantityField(var);
			select.addSelect(fld);
		}
	}

	private void selectMeasures(SelectQuery<?> select, FactTable factTable) {
		Entity entity = factTable.getEntity();
		List<VariableAggregate> aggs = entity.getVariableAggregates();
		OutputTable outputDataTable = (OutputTable) factTable.getSourceOutputTable();
		for (VariableAggregate agg : aggs) {
			if( !VariableAggregate.AGGREGATE_TYPE.PER_UNIT_AREA.equals(agg.getAggregateType()) ) {
				QuantitativeVariable var = agg.getVariable();
//				Field<BigDecimal> measureFld = factTable.getMeasureField(agg);
				Field<BigDecimal> measureFld = factTable.getVariableAggregateField(agg);
				Field<BigDecimal> valueFld = outputDataTable.getQuantityField(var);
				select.addSelect( valueFld.as(measureFld.getName()) );
			}
		}
	}

	private void selectDimensionsRecursive(SelectQuery<?> select, FactTable factTable, OutputTable outputTable) {
		Entity entity = outputTable.getEntity();
		OutputTable parentTable = (OutputTable) outputTable.getParentTable();
		if ( parentTable != null ) {
//			Entity parentEntity = parentTable.getEntity();
//			if ( parentEntity.isUnitOfAnalysis() ) {
			addJoin(select, outputTable);
			selectDimensionsRecursive(select, factTable, parentTable);
//			}
		}
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for (CategoricalVariable<?> var : variables) {
			Field<?> valueField = outputTable.getCategoryValueField(var);
			select.addSelect(valueField);
			Field<Integer> idField = factTable.getDimensionIdField(var);
			if ( idField != null ) {
				select.addSelect(DSL.value(null).cast(SQLDataType.INTEGER).as(idField.getName()));
			}
		}
	}
	
	private void addJoin(SelectQuery<?> select, OutputTable outputTable) {
		DataTable parentTable = outputTable.getParentTable();
		Field<Long> parentId = outputTable.getParentIdField();
		TableField<Record, Long> id = parentTable.getIdField();
		select.addJoin(parentTable, parentId.eq(id));
	}
}