/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.util.postgres.PostgresDataType.DOUBLEPRECISION;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.utils.StringUtils;

/**
 * Representation of the error table 
 * 
 * @author Mino Togna
 *
 */
public class ErrorTable extends DataTable {
	private static final long serialVersionUID = 1L;
	
	// constants used to format table and column names
	private static final String TABLE_NAME_FORMAT = "_error_%s_%s_%s";
	private static final String ABSOLUTE_ERROR = "absolute_error";
	private static final String RELATIVE_ERROR = "relative_error";
	private static final String VARIANCE = "variance";
	private static final String MEAN = "mean";
	private static final String TOTAL = "total";
	
	public static final String ABSOLUTE_ERROR_LABEL = "Absolute Error";
	public static final String RELATIVE_ERROR_LABEL = "Relative Error";
	public static final String VARIANCE_LABEL = "Variance";
	public static final String MEAN_LABEL = "Mean";
	public static final String TOTAL_LABEL = "Total";
	
	private String columnNameFormat = "";
	
	// instance variables 
	private QuantitativeVariable quantitativeVariable;
//	private Aoi aoi;
	private CategoricalVariable<?> categoricalVariable;
	private AoiLevel aoiLevel;
	
	// fields
//	@Deprecated
//	private Field<Integer> aoiField;
	private Field<Integer> categoryIdField;

	private Field<Double> meanQuantityVariance;
	private Field<Double> meanQuantityRelativeError;
	private Field<Double> meanQuantityAbsoluteError;
	
	private Field<Double> totalQuantityVariance;
	private Field<Double> totalQuantityRelativeError;
	private Field<Double> totalQuantityAbsoluteError;
	
	private Field<Integer> aggregateFactCountField;

	
	protected ErrorTable(QuantitativeVariable quantitativeVariable , AoiLevel aoiLevel , CategoricalVariable<?> categoricalVariable , Schema schema){
		super( quantitativeVariable.getEntity(), getTableName(quantitativeVariable , aoiLevel , categoricalVariable) , schema );
		
//		super( getTableName(quantitativeVariable , aoi , categoricalVariable) , schema );
		
		this.quantitativeVariable 	= quantitativeVariable;
		this.categoricalVariable 	= categoricalVariable;
//		this.aoi 					= aoi;
		this.aoiLevel 				= aoiLevel;
		this.columnNameFormat 		= "%s_"+this.quantitativeVariable.getId()+"_"+this.categoricalVariable.getId()+"_"+aoiLevel.getId()+"_%s";
		
		initFields();
		
	}
	
	private void initFields(){
		// mean_volume_vegetation_type_atlantis_absolute_error
		this.meanQuantityAbsoluteError	= createField( getFieldName(MEAN , ABSOLUTE_ERROR), DOUBLEPRECISION, this );
		this.meanQuantityRelativeError	= createField( getFieldName(MEAN , RELATIVE_ERROR), DOUBLEPRECISION, this );
		this.meanQuantityVariance		= createField( getFieldName(MEAN , VARIANCE), DOUBLEPRECISION, this );
		
		this.totalQuantityAbsoluteError = createField( getFieldName(TOTAL , ABSOLUTE_ERROR), DOUBLEPRECISION, this );
		this.totalQuantityRelativeError = createField( getFieldName(TOTAL , RELATIVE_ERROR), DOUBLEPRECISION, this );
		this.totalQuantityVariance		= createField( getFieldName(TOTAL , VARIANCE), DOUBLEPRECISION, this );
		
//		this.aoiField = createField( this.aoi.getAoiLevel().getFkColumn() , INTEGER, this);
		
		super.createAoiIdFields( this.aoiLevel );
		
		String fieldName = ( (MultiwayVariable) this.categoricalVariable ).getInputCategoryIdColumn();
		this.categoryIdField = createField(fieldName, SQLDataType.INTEGER, this);
		
		this.aggregateFactCountField = createField( AggregateTable.AGG_FACT_CNT_COLUMN, INTEGER, this );
	}

	private String getFieldName(String string1 , String string2){
		return String.format(columnNameFormat, string1 , string2);
	}

	private static String getTableName( QuantitativeVariable quantitativeVariable , AoiLevel aoiLevel , CategoricalVariable<?> categoricalVariable ){
		String name = String.format( TABLE_NAME_FORMAT, quantitativeVariable.getId() , categoricalVariable.getId() , aoiLevel.getId() );
		return StringUtils.normalize( name );
	}
	
	// getter methods
	public QuantitativeVariable getQuantitativeVariable() {
		return quantitativeVariable;
	}

	public CategoricalVariable<?> getCategoricalVariable() {
		return categoricalVariable;
	}
	
//	public Aoi getAoi() {
//		return aoi;
//	}

//	public Field<Integer> getAoiField() {
//		return aoiField;
//	}
	
	public Field<Integer> getCategoryIdField() {
		return categoryIdField;
	}
	
	public Field<Double> getMeanQuantityVariance() {
		return meanQuantityVariance;
	}

	public Field<Double> getMeanQuantityRelativeError() {
		return meanQuantityRelativeError;
	}

	public Field<Double> getMeanQuantityAbsoluteError() {
		return meanQuantityAbsoluteError;
	}

	public Field<Double> getTotalQuantityVariance() {
		return totalQuantityVariance;
	}

	public Field<Double> getTotalQuantityRelativeError() {
		return totalQuantityRelativeError;
	}

	public Field<Double> getTotalQuantityAbsoluteError() {
		return totalQuantityAbsoluteError;
	}
	
	public Field<Integer> getAggregateFactCountField() {
		return aggregateFactCountField;
	}

	public AoiLevel getAoiLevel() {
		return this.aoiLevel;
	}


}
