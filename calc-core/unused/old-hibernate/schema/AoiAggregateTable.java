package org.openforis.calc.schema;

import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class AoiAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_%s_agg";
//	private static final String AGG_FACT_CNT_COLUMN = "_agg_cnt";
	
//	private AoiLevel aoiHierarchyLevel;
//	private TableField<Record, Integer> aggregateFactCountField;
//	
//	private DataTable sourceTable;
//	private AoiLevel aoiLevel;
	
	AoiAggregateTable(DataTable sourceTable, AoiLevel aoiLevel) {
		super(sourceTable, getName(sourceTable.getEntity(), aoiLevel), aoiLevel );
	}
	
	private static String getName(Entity entity, AoiLevel aoiLevel) {
		return String.format(TABLE_NAME_FORMAT, entity.getName(), aoiLevel.getNormalizedName() );
	}

//	//	@Override
//	protected void initFields() {
//		// TODO Auto-generated method stub
////		dimensionIdFields = new HashMap<CategoricalVariable<?>, Field<Integer>>();
////		measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
//		
//		Entity entity = getEntity();
//		
////		this.categoryIdFields = factTable.categoryIdFields;
//		
////		createPrimaryKeyField();
//		createDimensionFieldsRecursive(entity);
//		createStratumField();
//		createAoiIdFields(this.aoiLevel);
//		createOutputQuantityFields(entity);
////		createQuantityFields(false, true);
//		
//		createAggregateFactCountField();
//	}
//	
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
	
//	private void createAggregateFactCountField() {
//		aggregateFactCountField = createField(AGG_FACT_CNT_COLUMN, INTEGER, this);
//	}
	
//	private static String getName(DataTable factTable, AoiLevel level) {
//		String entityName = factTable.getEntity().getName();
//		String levelName = level.getName();
//		return String.format(TABLE_NAME_FORMAT, entityName, levelName);
//	}

//	public AoiLevel getAoiHierarchyLevel() {
//		return aoiHierarchyLevel;
//	}

//
//	@Override
//	protected void createBinaryCategoryValueField(BinaryVariable var, String valueColumn) {
//		if ( var.isDisaggregate() ) {
//			super.createBinaryCategoryValueField(var, valueColumn);
//		}
//	}
	
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
//
//	public DataTable getSourceTable() {
//		return sourceTable;
//	}
//	
//	public AoiLevel getAoiLevel() {
//		return aoiLevel;
//	}
//	
//	public AoiHierarchy getAoiHierarchy() {
//		return this.aoiLevel.getHierarchy();
//	}
}
