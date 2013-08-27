/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * @author G. Miceli
 * 
 */
public class FactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String FACT_TABLE_NAME_FORMAT = "_%s_fact";
	private static final String DIMENSION_ID_COLUMN_FORMAT = "%s_id";
	
	private Map<VariableAggregate, Field<BigDecimal>> measureFields;
	private Map<CategoricalVariable, Field<Integer>> dimensionIdFields;

	FactTable(Entity entity, OutputSchema schema, OutputDataTable sourceTable, FactTable parentTable) {
		super(entity, getName(entity), schema, sourceTable, parentTable);
		createDimensionFields(entity);
		createStratumIdField();
		createAoiIdFields();
		createQuantityFields(false);
		createMeasureFields(entity);
		createParentIdField();
	}

	/**
	 * Recursively up to root unit of analysis
	 */
	private void createDimensionFields(Entity entity) {
		Entity parent = entity.getParent();
		if ( parent != null && parent.isUnitOfAnalysis() ) {
			createDimensionFields(parent);
		}
		createDimensionIdFields(entity);
		createCategoryValueFields(entity, false);
	}

	private void createDimensionIdFields(Entity entity) {
		this.dimensionIdFields = new HashMap<CategoricalVariable, Field<Integer>>();
		List<Variable> variables = entity.getVariables();
		for ( Variable var : variables ) {
			if ( var instanceof CategoricalVariable ) {
				createDimensionIdField((CategoricalVariable) var);
			}
		}
	}

	private void createDimensionIdField(CategoricalVariable var) {
		if ( !var.isDegenerateDimension() ) {
			String fieldName = String.format(DIMENSION_ID_COLUMN_FORMAT, var.getName());
			Field<Integer> fld = createField(fieldName, SQLDataType.INTEGER, this);
			dimensionIdFields.put(var, fld);
		}
	}

	private void createMeasureFields(Entity entity) {
		this.measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
		List<VariableAggregate> aggregates = entity.getVariableAggregates();
		for (VariableAggregate agg : aggregates) {
			Field<BigDecimal> field = createField(agg.getName(), Psql.DOUBLE_PRECISION, this);
			measureFields.put(agg, field);
		}
	}

	private static String getName(Entity entity) {
		return String.format(FACT_TABLE_NAME_FORMAT, entity.getDataTable());
	}
	
	public Field<BigDecimal> getMeasureField(VariableAggregate aggregate) {
		return measureFields.get(aggregate);
	}
	
	public Field<Integer> getDimensionIdField(CategoricalVariable variable) {
		return dimensionIdFields.get(variable);
	}
}
