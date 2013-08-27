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
import org.openforis.calc.persistence.postgis.Psql;
import org.openforis.calc.schema.DataTable;
import org.openforis.calc.schema.FactTable;
import org.openforis.calc.schema.OutputDataTable;
import org.openforis.calc.schema.OutputSchema;

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
			OutputDataTable outputTable = (OutputDataTable) factTable.getSourceTable();
			
			SelectQuery<?> select = new Psql().selectQuery(outputTable);
			select.addSelect(outputTable.getIdField());
			selectDimensionsRecursive(select, factTable, outputTable);
			select.addSelect(outputTable.getStratumIdField());
			select.addSelect(outputTable.getAoiIdFields());
			selectQuantities(select, outputTable);
			selectMeasures(select, factTable);
			
//			if ( isDebugMode() ) {
//				psql()
//					.dropTableIfExists(factTable)
//					.execute();
//			}
//			
//			psql()
//				.createTable(factTable)
//				.as(select)
//				.execute();
			System.out.println(select);
		}
	}

	private void selectQuantities(SelectQuery<?> select, OutputDataTable outputTable) {
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
		OutputDataTable outputDataTable = (OutputDataTable) factTable.getSourceTable();
		for (VariableAggregate agg : aggs) {
			QuantitativeVariable var = agg.getVariable();
			Field<BigDecimal> measureFld = factTable.getMeasureField(agg);
			Field<BigDecimal> valueFld = outputDataTable.getQuantityField(var);
			select.addSelect(valueFld.as(measureFld.getName()));
		}
	}

	private void selectDimensionsRecursive(SelectQuery<?> select, FactTable factTable, OutputDataTable outputTable) {
		Entity entity = outputTable.getEntity();
		OutputDataTable parentTable = (OutputDataTable) outputTable.getParentTable();
		if ( parentTable != null ) {
			Entity parentEntity = parentTable.getEntity();
			if ( parentEntity.isUnitOfAnalysis() ) {
				selectDimensionsRecursive(select, factTable, parentTable);
				addJoin(select, outputTable);
			}
		}
		List<CategoricalVariable> variables = entity.getCategoricalVariables();
		for (CategoricalVariable var : variables) {
			Field<?> valueField = outputTable.getCategoryValueField(var);
			select.addSelect(valueField);
			Field<Integer> idField = factTable.getDimensionIdField(var);
			if ( idField != null ) {
				select.addSelect(DSL.value(null).cast(SQLDataType.INTEGER).as(idField.getName()));
			}
		}
	}
	
	private void addJoin(SelectQuery<?> select, OutputDataTable outputTable) {
		DataTable parentTable = outputTable.getParentTable();
		Field<Integer> parentId = outputTable.getParentIdField();
		TableField<Record, Integer> id = parentTable.getIdField();
		select.addJoin(parentTable, parentId.eq(id));
	}
}