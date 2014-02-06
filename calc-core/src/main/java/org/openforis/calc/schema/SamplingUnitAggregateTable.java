package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author Mino Togna
 * 
 */
public class SamplingUnitAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;
	private static final String AGG_FACT_CNT_COLUMN = "_agg_cnt";
	
//	private AoiLevel aoiHierarchyLevel;
//	private TableField<Record, Integer> aggregateFactCountField;

	SamplingUnitAggregateTable(DataTable factTable) {
		super(factTable, getName(factTable));
	}
	
//	@Override
//	protected void initFields() {
//		// TODO Auto-generated method stub
////		dimensionIdFields = new HashMap<CategoricalVariable<?>, Field<Integer>>();
////		measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
//		
//		Entity entity = this.sourceFactTable.getEntity();
//		
////		this.categoryIdFields = factTable.categoryIdFields;
////		
////		createPrimaryKeyField();
//		createDimensionFieldsRecursive(entity);
//		createStratumField();
//		createAoiIdFields();
//		createOutputQuantityFields(entity);
////		createQuantityFields(false, true);
//		
//		createAggregateFactCountField();
//	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	protected void createPrimaryKeyField() {
//		setIdField( createField(getEntity().getParentIdColumn(), BIGINT, this) );
//		setPrimaryKey( KeyFactory.newUniqueKey(this, getIdField()) );
//	}

//	private void createOutputQuantityFields(Entity entity) {
//		// create measure for each aggregate
//		for (QuantitativeVariable variable : entity.getOutputVariables() ) {
//			
//			createQuantityField(variable, variable.getName());
//			
//			for (VariableAggregate agg : variable.getAggregates()) {
//				String columnName = String.format("%s_%s", variable.getName(), agg.getAggregateType());
//				TableField<Record,BigDecimal> field = super.createField(columnName, Psql.DOUBLE_PRECISION, this);
//				
//				addVariableAggregateField(agg, field);
//			}
//		}
//		
//	}

	private static String getName(DataTable factTable) {
		String entityName = factTable.getEntity().getName();
		return String.format( "_%s_plot_agg", entityName);
	}

//	public AoiLevel getAoiHierarchyLevel() {
//		return aoiHierarchyLevel;
//	}

//	private void createAggregateFactCountField() {
//		aggregateFactCountField = createField(AGG_FACT_CNT_COLUMN, INTEGER, this);
//	}

//	@Override
//	protected void createBinaryCategoryValueField(BinaryVariable var, String valueColumn) {
//		if ( var.isDisaggregate() ) {
//			super.createBinaryCategoryValueField(var, valueColumn);
//		}
//	}
//	
//	@Override
//	protected void createCategoryValueField(MultiwayVariable var, String valueColumn) {
//		if ( var.isDisaggregate() ) {
//			super.createCategoryValueField(var, valueColumn);
//		}
//	}
	
//	@Override
//	protected void createDimensionIdField(CategoricalVariable<?> var) {
//		if ( var.isDisaggregate() ) {
//			super.createDimensionIdField(var);
//		}
//	}
	
//	public TableField<Record, Integer> getAggregateFactCountField() {
//		return aggregateFactCountField;
//	}

//	public FactTable getSourceFactTable() {
//		return sourceFactTable;
//	}
}
