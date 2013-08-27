/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;

/**
 * @author G. Miceli
 * 
 */
public class FactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_fact";
	private static final String DIMENSION_ID_COLUMN_FORMAT = "%s_id";
	
	private Map<VariableAggregate, Field<BigDecimal>> measureFields;
	private Map<CategoricalVariable, Field<Integer>> dimensionIdFields;

	protected FactTable(Entity entity, String name, Schema schema, DataTable sourceTable, FactTable parentTable) {
		super(entity, name, schema, sourceTable, parentTable);
	}
	
	FactTable(Entity entity, Schema schema, OutputDataTable sourceTable, FactTable parentTable) {
		this(entity, getName(entity), schema, sourceTable, parentTable);
		createPrimaryKeyField();
		createDimensionFields(entity);
		createStratumIdField();
		createAoiIdFields(null);
		createQuantityFields(false);
		createMeasureFields(entity);
		createParentIdField();
	}

	/**
	 * Recursively up to root unit of analysis
	 */
	protected void createDimensionFields(Entity entity) {
		Entity parent = entity.getParent();
		if ( parent != null && parent.isUnitOfAnalysis() ) {
			createDimensionFields(parent);
		}
		createDimensionIdFields(entity);
		createCategoryValueFields(entity, false, true);
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
		if ( !var.isDegenerateDimension() && var.isDisaggregate() ) {
			String fieldName = String.format(DIMENSION_ID_COLUMN_FORMAT, var.getName());
			Field<Integer> fld = createField(fieldName, SQLDataType.INTEGER, this);
			dimensionIdFields.put(var, fld);
		}
	}

	protected void createMeasureFields(Entity entity) {
		this.measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
		List<VariableAggregate> aggregates = entity.getVariableAggregates();
		for (VariableAggregate agg : aggregates) {
			Field<BigDecimal> field = createField(agg.getName(), Psql.DOUBLE_PRECISION, this);
			measureFields.put(agg, field);
		}
	}

	private static String getName(Entity entity) {
		return String.format(TABLE_NAME_FORMAT, entity.getDataTable());
	}
	
	public Field<BigDecimal> getMeasureField(VariableAggregate aggregate) {
		return measureFields.get(aggregate);
	}
	
	public Field<Integer> getDimensionIdField(CategoricalVariable variable) {
		return dimensionIdFields.get(variable);
	}

	public Collection<Field<Integer>> getDimensionIdFields() {
		return Collections.unmodifiableCollection(dimensionIdFields.values());
	}
//
//	@SuppressWarnings("unchecked")
//	public Collection<Field<?>> getAoiIdFields(final AoiHierarchyLevel lowestLevel) {
//		return CollectionUtils.select(getAoiIdFields(), new Predicate() {
//			@Override
//			public boolean evaluate(Object object) {
//				AoiHierarchyLevel level = (AoiHierarchyLevel) object;
//				return level.getRank() <= lowestLevel.getRank();
//			}
//		});
//	}
}
