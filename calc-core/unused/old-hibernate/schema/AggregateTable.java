package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class AggregateTable extends DataTable {

	private static final long serialVersionUID = 1L;
//	private static final String TABLE_NAME_FORMAT = "_%s_%s_stratum_agg";
	private static final String AGG_FACT_CNT_COLUMN = "_agg_cnt";
	
	private AoiLevel aoiLevel;
	private TableField<Record, Integer> aggregateFactCountField;
	private DataTable sourceTable;

	AggregateTable(DataTable sourceTable, String name, AoiLevel aoiLevel) {
		super(sourceTable.getEntity(), name, sourceTable.getSchema());
		
		this.sourceTable = sourceTable;
		this.aoiLevel = aoiLevel;
		
		initFields();
	}
	
	AggregateTable(DataTable sourceTable, String name) {
		this( sourceTable, name, null );
	}
	
//	@Override
	protected void initFields() {
		Entity entity = getEntity();
		
		createDimensionFieldsRecursive(entity);
		createStratumField();
		createAoiIdFields( aoiLevel );
		createOutputQuantityFields(entity);
		createAggregateFactCountField();
	}
	
	private void createOutputQuantityFields(Entity entity) {
		// create measure for each aggregate
		for (QuantitativeVariable variable : entity.getOutputVariables() ) {
			
			createQuantityField(variable, variable.getName());
			
			for (VariableAggregate agg : variable.getAggregates()) {
				String columnName = String.format("%s_%s", variable.getName(), agg.getAggregateType());
				TableField<Record,BigDecimal> field = super.createField(columnName, Psql.DOUBLE_PRECISION, this);
				
				addVariableAggregateField(agg, field);
			}
		}
		
	}
	
	private void createAggregateFactCountField() {
		aggregateFactCountField = createField(AGG_FACT_CNT_COLUMN, INTEGER, this);
	}
	
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
	
	public TableField<Record, Integer> getAggregateFactCountField() {
		return aggregateFactCountField;
	}

	public DataTable getSourceTable() {
		return sourceTable;
	}
	
	public AoiLevel getAoiLevel() {
		return aoiLevel;
	}
	
	public AoiHierarchy getAoiHierarchy() {
		return this.aoiLevel.getHierarchy();
	}

}
