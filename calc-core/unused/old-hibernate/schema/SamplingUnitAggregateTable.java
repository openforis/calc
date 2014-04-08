package org.openforis.calc.schema;


/**
 * 
 * @author Mino Togna
 * 
 */
public class SamplingUnitAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;
//	private static final String AGG_FACT_CNT_COLUMN = "_agg_cnt";
	
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
