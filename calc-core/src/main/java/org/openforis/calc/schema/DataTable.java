/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.BIGINT;
import static org.jooq.impl.SQLDataType.BOOLEAN;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.collect.SpeciesCodeTable;
import org.openforis.calc.collect.SpeciesCodeView;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.CategoryLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.metadata.TextVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.GeodeticCoordinate;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;

/**
 * A table derived from a Calc entity; this includes input and output data tables as well as OLAP fact tables
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class DataTable extends AbstractTable {

	public static final String WEIGHT_COLUMN = "weight";

	private static final long serialVersionUID = 1L;

	private Entity entity;
	private UniqueKey<Record> primaryKey;
	
	private Map<AoiLevel, Field<Integer>> aoiIdFields;
	private Map<QuantitativeVariable, Field<BigDecimal>> quantityFields;
	
	// are these used?
	private Map<CategoricalVariable<?>, Field<?>> categoryValueFields;
	protected Map<CategoricalVariable<?>, Field<Integer>> categoryIdFields;
	
	private Map<VariableAggregate, Field<BigDecimal>> variableAggregateFields;
	private Map<TextVariable, Field<String>> textFields;
	
	private TableField<Record, Long> idField;
	private Field<GeodeticCoordinate> locationField;
	private Field<BigDecimal> xField;
	private Field<BigDecimal> yField;
	private Field<String> srsIdField;
	private Field<Long> parentIdField;
	private Field<Long> samplingUnitIdField;

	private Map<CategoricalVariable<?>, Field<Integer>> dimensionIdFields;
	private Map<CategoricalVariable<?>, Field<Integer>> speciesDimensionIdFields;
	private Map<MultiwayVariable, List<Field<Integer>> > speciesDimensionFields;
	private Field<Integer> stratumField;
	private Field<Integer> clusterField;
	
	private Field<BigDecimal> weightField;

	private List<Field<?>> psuFields;

	
	protected DataTable(Entity entity, String name, Schema schema) {
		super(name, schema);
		this.entity 					= entity;
		this.aoiIdFields 				= new HashMap<AoiLevel, Field<Integer>>();
		this.quantityFields 			= new HashMap<QuantitativeVariable, Field<BigDecimal>>();
		this.categoryValueFields 		= new HashMap<CategoricalVariable<?>, Field<?>>();
		this.categoryIdFields 			= new HashMap<CategoricalVariable<?>, Field<Integer>>();
		this.variableAggregateFields 	= new HashMap<VariableAggregate, Field<BigDecimal>>();
		this.textFields 				= new HashMap<TextVariable, Field<String>>();
		this.dimensionIdFields 			= new HashMap<CategoricalVariable<?>, Field<Integer>>();
		this.speciesDimensionIdFields	= new HashMap<>();
		this.speciesDimensionFields 	= new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	protected void createPrimaryKeyField() {
		this.idField = createField(entity.getIdColumn(), BIGINT, this);
		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
	}
	
	protected void setIdField(TableField<Record, Long> idField) {
		this.idField = idField;
	}
	
	protected void setPrimaryKey(UniqueKey<Record> primaryKey) {
		this.primaryKey = primaryKey;
	}

	protected void createQuantityField(QuantitativeVariable var, String valueColumn) {
		Field<BigDecimal> field = createValueField(var, Psql.DOUBLE_PRECISION, valueColumn);
		quantityFields.put(var, field);
	}
	
	protected void createQuantityFields(boolean input) {
		createQuantityFields(input, false);
	}
	
	protected void createQuantityFields(boolean input, boolean variableAggregates) {
		Entity entity = getEntity();
		createQuantityFields(entity, input, variableAggregates);
	}

	protected void createQuantityFields(Entity entity, boolean input, boolean variableAggregates) {
		List<QuantitativeVariable> variables = entity.getQuantitativeVariables();
		for ( QuantitativeVariable var : variables ) {
			createQuantityField(var, input, variableAggregates);
		}
	}

	private void createQuantityField(QuantitativeVariable var, boolean input, boolean variableAggregates) {
		String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
		if ( StringUtils.isNotBlank(valueColumn) ) {
			createQuantityField(var, valueColumn);
		}
		if( variableAggregates ) {
			createVariableAggregateFields(var);
		}
		
		// create columns also for the variable-per-ha if present
		QuantitativeVariable variablePerHa = var.getVariablePerHa();
		if( variablePerHa != null ){
			createQuantityField(variablePerHa, input, variableAggregates);
		}
	}

	protected void createVariableAggregateFields(QuantitativeVariable var) {
		List<VariableAggregate> aggregates = var.getAggregates();
		for ( VariableAggregate varAgg : aggregates ) {
			Field<BigDecimal> field = createField(varAgg.getName(), Psql.DOUBLE_PRECISION, this);
			addVariableAggregateField(varAgg, field);
		}
	}

	protected void addVariableAggregateField(VariableAggregate varAgg, Field<BigDecimal> field) {
		variableAggregateFields.put(varAgg, field);
	}
	
	protected void createCategoryIdFields(Entity entity, boolean input) {
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable<?> var : variables ) {
			// only input categories
			if( !var.isUserDefined() || entity.getDefaultProcessingChainCategoricalOutputVariables().contains(var)){
				String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
				if ( valueColumn != null ) {
					if ( var instanceof BinaryVariable ) {
	//					createBinaryCategoryValueField((BinaryVariable) var, valueColumn);
					} else if ( var instanceof MultiwayVariable ) {
						createCategoryIdField((MultiwayVariable) var, ((MultiwayVariable) var).getInputCategoryIdColumn());
					}
				}
			}
		}
	}
	
	protected void createCategoryIdField(MultiwayVariable var, String valueColumn) {
		Field<Integer> fld = createValueField(var,SQLDataType.INTEGER, valueColumn);
		categoryIdFields.put(var, fld);
	}
	
	protected void createCategoryValueFields(Entity entity, boolean input) {
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable<?> var : variables ) {
			// only input categories
			if( !var.isUserDefined() || entity.getDefaultProcessingChainCategoricalOutputVariables().contains(var)){
				String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
				if ( valueColumn != null ) {
					if ( var instanceof BinaryVariable ) {
						createBinaryCategoryValueField((BinaryVariable) var, valueColumn);
					} else if ( var instanceof MultiwayVariable ) {
						createCategoryValueField((MultiwayVariable) var, valueColumn);
					}
				}
			}
		}
	}

	protected void createCategoryValueField(MultiwayVariable var, String valueColumn) {
		Field<String> fld = createValueField(var, VARCHAR.length(255), valueColumn);
		categoryValueFields.put(var, fld);
	}

	protected void createBinaryCategoryValueField(BinaryVariable var, String valueColumn) {
		Field<Boolean> fld = createValueField((BinaryVariable) var, BOOLEAN, valueColumn);
		categoryValueFields.put(var, fld);
	}

	protected <T> Field<T> createValueField(Variable<?> var, DataType<T> valueType, String valueColumn) {
		if ( valueColumn == null ) {
			return null;
		} else {
			return createField(valueColumn, valueType, this);
		}
	}

	public TableField<Record, Long> getIdField() {
		return idField;
	}

	public Entity getEntity() {
		return entity;
	}
	
	public Workspace getWorkspace(){
		return entity.getWorkspace();
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}

	public Field<Integer> getAoiIdField(AoiLevel level) {
		return aoiIdFields == null ? null : aoiIdFields.get(level);
	}

	public Field<GeodeticCoordinate> getLocationField() {
		return locationField;
	}

	public Field<BigDecimal> getXField() {
		return xField;
	}
	
	public Field<BigDecimal> getYField() {
		return yField;
	}
	
	public Field<String> getSrsIdField() {
		return srsIdField;
	}
	
	protected void createAoiIdFields() {
		createAoiIdFields(null);
	}
	
	protected void createAoiIdFields(AoiLevel lowestLevel) {
		if ( isGeoreferenced() ) {
			Workspace workspace = entity.getWorkspace();
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			for ( AoiHierarchy hierarchy : aoiHierarchies ) {
				List<AoiLevel> levels = hierarchy.getLevels();
				for ( AoiLevel level : levels ) {
					if ( lowestLevel == null || level.getRank() <= lowestLevel.getRank() ) {
						String fkColumn = level.getFkColumn();
						createAoiIdField(level, fkColumn);
					}
				}
			}
		}
	}

	protected void createAoiIdField(AoiLevel level, String fkColumn) {
		Field<Integer> aoiField = createField(fkColumn, INTEGER, this);
		aoiIdFields.put(level, aoiField);
	}

	protected void createLocationField() {
		if ( isGeoreferenced() ) {
			locationField = createField("_location", Psql.GEODETIC_COORDINATE, this);
		}
	}

	protected void createCoordinateFields() {
		if ( isGeoreferenced() ) {
			String xColumn = entity.getXColumn();
			String yColumn = entity.getYColumn();
			String srsColumn = entity.getSrsColumn();

			if ( !(StringUtils.isBlank(xColumn) || StringUtils.isBlank(yColumn) || StringUtils.isBlank(srsColumn)) ) {
				xField = createField(xColumn, Psql.DOUBLE_PRECISION, this);
				yField = createField(yColumn, Psql.DOUBLE_PRECISION, this);
				srsIdField = createField(srsColumn, SQLDataType.VARCHAR, this);
			}
		}
	}

	protected void createParentIdField() {
		String parentIdColumn = entity.getParentIdColumn();
		if ( parentIdColumn == null ) {
			if ( entity.getParent() != null ) {
				throw new NullPointerException("parent_id_column not defined for entity "+entity);
			}
		} else {
			this.parentIdField = createField(parentIdColumn, BIGINT, this);
		}
	}
	
	protected void createSamplingUnitIdField() {
		if( entity.isInSamplingUnitHierarchy() ){
			Entity samplingUnit = entity.getWorkspace().getSamplingUnit();
			String suIdColumn 	= samplingUnit.getIdColumn();
			this.samplingUnitIdField = createField( suIdColumn, BIGINT, this );
		}
	}
	
	protected void createTextFields() {
		Entity entity = getEntity();
		createTextFields(entity);
	}

	protected void createTextFields(Entity entity) {
		List<TextVariable> vars = entity.getTextVariables();
		for ( TextVariable var : vars ) {
			String name = var.getInputValueColumn();
			Field<String> fld = createField(name, VARCHAR.length(255), this);
			textFields.put(var, fld);
		}
	}

	public Field<Long> getParentIdField() {
		return parentIdField;
	}
	
	public Field<Long> getSamplingUnitIdField() {
		return samplingUnitIdField;
	}

	public Collection<Field<?>> getCategoryValueFields() {
		return Collections.unmodifiableCollection(categoryValueFields.values());
	}
	
	public Collection<Field<Integer>> getCategoryIdFields() {
		return Collections.unmodifiableCollection(categoryIdFields.values());
	}
	
	public Collection<Field<Integer>> getAoiIdFields() {
		return Collections.unmodifiableCollection(aoiIdFields.values());
	}
	
	public Collection<Field<String>> getTextFields() {
		return Collections.unmodifiableCollection(textFields.values());
	}
	
	public Field<String> getTextField(TextVariable var){
		return this.textFields.get( var );
	}
	
	public Field<BigDecimal> getQuantityField(QuantitativeVariable var) {
		return quantityFields == null ? null : quantityFields.get(var);
	}
	
	public Field<?> getCategoryValueField(CategoricalVariable<?> var) {
		return categoryValueFields.get(var);
	}
	
	public Field<Integer> getCategoryIdField(CategoricalVariable<?> var) {
		return categoryIdFields.get(var);
	}
	
	public Field<BigDecimal> getVariableAggregateField(VariableAggregate variableAggregate){
		return variableAggregateFields.get(variableAggregate);
	}
	
	public boolean isGeoreferenced() {
		return getEntity().isGeoreferenced();
	}

	protected void createDimensionFieldsRecursive(Entity entity) {
//		this.dimensionIdFields = new HashMap<CategoricalVariable<?>, Field<Integer>>();
		List<CategoricalVariable<?>> dimensions = entity.getDimensions();
		for (CategoricalVariable<?> var : dimensions) {
			if( var instanceof MultiwayVariable ){
				String fieldName = ( (MultiwayVariable) var ).getInputCategoryIdColumn();  // String.format(DIMENSION_ID_COLUMN_FORMAT, var.getName());
				Field<Integer> fld = createField(fieldName, SQLDataType.INTEGER, this);
				dimensionIdFields.put(var, fld);
			}
		}
		
	}

	protected void createStratumField() {
		if( getEntity().getWorkspace().hasStratifiedSamplingDesign() ){
		
			if( entity.isInSamplingUnitHierarchy() ){
				this.stratumField = createField("_stratum", INTEGER, this);	
			}
			
//			Entity parent = this.entity.getParent();
//			if( entity.isSamplingUnit() || (parent != null && parent.isSamplingUnit()) ) {
//				this.stratumField = createField("_stratum", INTEGER, this);
//			}

		}
	}
	
	protected void createClusterField() {
		if( getWorkspace().hasClusterSamplingDesign() ){
			Entity clusterEntity = getWorkspace().getSamplingDesign().getClusterEntity();
			this.clusterField = createField( clusterEntity.getIdColumn() , SQLDataType.INTEGER, this );
		}
	}
	
	protected void createSpeciesDimensionFields() {
//		this.dimensionIdFields = new HashMap<CategoricalVariable<?>, Field<Integer>>();
//		List<CategoricalVariable<?>> dimensions = entity.getDimensions();
		for (CategoricalVariable<?> var : entity.getSpeciesCategoricalVariables()) {
			if( var instanceof MultiwayVariable && ((MultiwayVariable) var).isSpecieCategory() ){
				
				CategoryLevel level 	= var.getCategoryLevel();
				SpeciesCodeTable table 	= new SpeciesCodeTable( level.getName() , this.getSchema().getName() );
				SpeciesCodeView view	= new SpeciesCodeView( table );
				
				List<Field<Integer>> fields = new ArrayList<>();
				
				Field<Integer> fld 		= createField( view.getIdField().getName() , SQLDataType.INTEGER, this );
				fields.add(fld);
				
				Field<Integer> fldGenus = createField( view.getGenusIdField().getName() , SQLDataType.INTEGER, this );
				fields.add(fldGenus);
				
				speciesDimensionFields.put( (MultiwayVariable) var, fields );
				speciesDimensionIdFields.put( (MultiwayVariable) var, fld );
			}
		}
		
	}
	
	public Collection<Field<Integer>> getDimensionIdFields() {
		return Collections.unmodifiableCollection(dimensionIdFields.values());
	}
	
	public Field<Integer> getDimensionIdField(CategoricalVariable<?> variable) {
		return dimensionIdFields.get(variable);
	}

	public Collection<Field<Integer>> getSpeciesDimensionFields() {
		List<Field<Integer>> fields = new ArrayList<>();
		for (List<Field<Integer>> list : speciesDimensionFields.values()) {
			fields.addAll(list);
		}
		return Collections.unmodifiableCollection( fields );
	}
	
	public Field<Integer> getSpeciesDimensionIdField(CategoricalVariable<?> variable) {
		return speciesDimensionIdFields.get(variable);
	}
	
	public Field<Integer> getStratumField() {
		return stratumField;
	}

	public Field<Integer> getClusterField() {
		return clusterField;
	}
	
	protected void createWeightField() {
		if( getEntity().isInSamplingUnitHierarchy() ) {
			weightField = createField( WEIGHT_COLUMN, Psql.DOUBLE_PRECISION, this );
		}
	}

	public Field<BigDecimal> getWeightField() {
		return weightField;
	}

	protected void createPsuFields() {
		if( getWorkspace().has2StagesSamplingDesign() ){

			SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
			List<ColumnJoin> columns = samplingDesign.getTwoStagesSettingsObject().getSamplingUnitPsuJoinColumns();
			for (ColumnJoin columnJoin : columns) {
				Field<?> field = field( columnJoin.getColumn() );
				addPsuField(field);
			}
		}
		
	}

	protected void addPsuField(Field<?> field) {
		if( this.psuFields == null ){
			this.psuFields = new ArrayList<Field<?>>();
		}
		this.psuFields.add( field );
	}
	
	
	
	public List<Field<?>> getPsuFields() {
		return CollectionUtils.unmodifiableList( psuFields );
	}
	
}
