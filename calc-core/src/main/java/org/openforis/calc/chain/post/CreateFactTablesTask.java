package org.openforis.calc.chain.post;

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
import org.openforis.calc.psql.Psql;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.OutputSchema;
import org.openforis.calc.schema.OutputTable;

/**
 * Creates and populates fact tables for entities marked "unit of analysis"
 * 
 * @author G. Miceli
 */
public final class CreateFactTablesTask extends Task {
	@Override
	protected void execute() throws Throwable {
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
		Field<Integer> parentId = outputTable.getParentIdField();
		TableField<Record, Integer> id = parentTable.getIdField();
		select.addJoin(parentTable, parentId.eq(id));
	}
}