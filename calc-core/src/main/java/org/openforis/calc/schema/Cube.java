package org.openforis.calc.schema;
import static org.openforis.calc.schema.ErrorTable.ABSOLUTE_ERROR_LABEL;
import static org.openforis.calc.schema.ErrorTable.MEAN_LABEL;
import static org.openforis.calc.schema.ErrorTable.RELATIVE_ERROR_LABEL;
import static org.openforis.calc.schema.ErrorTable.TOTAL_LABEL;
import static org.openforis.calc.schema.ErrorTable.VARIANCE_LABEL;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Field;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.schema.Measure.AGGREGATE_FUNCTION;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class Cube {

	private Map<Dimension, Field<Integer>> dimensionUsages;
	private Map<AoiDimension, Field<Integer>> aoiDimensionUsages;
	private Map<Measure, Field<BigDecimal>> measures;
	private List<Measure> errorMeasures;
	
	private RolapSchema rolapSchema;
	private FactTable factTable;

	private String name;
	private String schema;
	private String table;
	private List<AggName> aggNames;

	private String errorColumnNameFormat 	= "%s %s";
	private String errorColumnCaptionFormat = "%s (%s) %s";
	private String areaErrorColumnCaptionFormat = "%s %s";
	
	Cube(RolapSchema rolapSchema, FactTable factTable) {
		Entity entity = factTable.getEntity();
		this.name = entity.getName();
		if( entity.getParent() != null && entity.getParent().isInSamplingUnitHierarchy() ){
			this.name += "_"; 
		}
		
		this.factTable = factTable;
		this.rolapSchema = rolapSchema;

		this.table = factTable.getName();
		this.schema = rolapSchema.getDataSchema().getName();

		createAoiDimensionUsages();
		createDimensionUsages();
		createMeasures();
		createAggNames();
	}



	private AoiDimension getAoiDimension(AoiHierarchy aoiHierarchy) {
		for ( AoiDimension aoiDimension : aoiDimensionUsages.keySet() ) {
			if ( aoiDimension.getAoiHierarchy().equals(aoiHierarchy) ) {
				return aoiDimension;
			}
		}
		throw new IllegalArgumentException("Unable to find aoi dimension for aoi hierarchy " + aoiHierarchy.getName());
	}

	private void createAoiDimensionUsages() {
		this.aoiDimensionUsages = new HashMap<AoiDimension, Field<Integer>>();

		if ( factTable.isGeoreferenced() ) {
			List<AoiDimension> aoiDimensions = rolapSchema.getAoiDimensions();
			for ( AoiDimension aoiDimension : aoiDimensions ) {
				AoiHierarchy aoiHierarchy = aoiDimension.getAoiHierarchy();
				AoiLevel leafLevel = aoiHierarchy.getLeafLevel();
				Field<Integer> aoiIdField = factTable.getAoiIdField(leafLevel);

				aoiDimensionUsages.put(aoiDimension, aoiIdField);
			}
		}
	}

	private void createDimensionUsages() {
 		this.dimensionUsages = new HashMap<Dimension, Field<Integer>>();

		Map<CategoricalVariable<?>, CategoryDimension> dimensionsMap = rolapSchema.getSharedDimensionsMap();
		for ( CategoricalVariable<?> var : dimensionsMap.keySet() ) {
			Field<Integer> field = factTable.getDimensionIdField(var);
			if ( field != null ) {
				Dimension dim = dimensionsMap.get(var);
				dimensionUsages.put(dim, field);
			}
		}
		
		Map<CategoricalVariable<?>, SpeciesCategoryDimension> speciesDimensionsMap = rolapSchema.getSpeciesDimensionsMap();
		for ( CategoricalVariable<?> var : speciesDimensionsMap.keySet() ) {
			Field<Integer> field = factTable.getSpeciesDimensionIdField(var);
			if ( field != null ) {
				Dimension dim = speciesDimensionsMap.get(var);
				dimensionUsages.put(dim, field);
			}
		}
		
	}

	private void createMeasures() {
		this.measures 		= new HashMap<Measure, Field<BigDecimal>>();
		this.errorMeasures 	= new ArrayList<Measure>();
		Entity entity 		= factTable.getEntity();

		Map<QuantitativeVariable, CalculationStep> stepMap = entity.getDefaultProcessingChainCalculationStepQuantitativeVariablesMap();
		for ( QuantitativeVariable var : stepMap.keySet() ){
			CalculationStep calcStep = stepMap.get( var );
			Set<String> aggregateFunctions = calcStep.getAggregateFunctions();
			
			Field<BigDecimal> measureField = factTable.getMeasureField(var);
			String measureColumn = measureField.getName();
			if( entity.isInSamplingUnitHierarchy() || aggregateFunctions.isEmpty() ){
				
				Measure measure = new Measure( getRolapSchema(), this, var , measureColumn );
				measures.put(measure, measureField);
				
			} else {
				
				for (String aggregateFunction : aggregateFunctions) {
					String name = var.getName() + " (" + aggregateFunction + ")";
					String caption = ( (StringUtils.isBlank(var.getCaption()) ) ? var.getName() : var.getCaption() ) + " (" + aggregateFunction + ")";
					Measure measure = new Measure( getRolapSchema(), this, name , caption, measureColumn, AGGREGATE_FUNCTION.getEnum(aggregateFunction) );
					measures.put(measure, measureField);
				}
				
			}
		
			// add error measures in case at least 1 error table has been defined for the given variable
			List<ErrorTable> errorTables = factTable.getErrorTables( var );
			if( errorTables.size() > 0 ){
				ErrorTable errorTable = errorTables.get( 0 );
				
				// relative error measures
				QuantitativeVariable quantitativeVariable = errorTable.getQuantitativeVariable();
				
				String measureName		= getErrorMeasureName( quantitativeVariable, RELATIVE_ERROR_LABEL , true );
				String measureCaption 	= getErrorMeasureCaption( quantitativeVariable, RELATIVE_ERROR_LABEL , MEAN_LABEL );
				Measure measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getMeanQuantityRelativeError().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				measureName 	= getErrorMeasureName( quantitativeVariable, ABSOLUTE_ERROR_LABEL , true );
				measureCaption 	= getErrorMeasureCaption( quantitativeVariable, ABSOLUTE_ERROR_LABEL , MEAN_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getMeanQuantityAbsoluteError().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				measureName 	= getErrorMeasureName( quantitativeVariable,  VARIANCE_LABEL , true );
				measureCaption 	= getErrorMeasureCaption( quantitativeVariable, VARIANCE_LABEL , MEAN_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getMeanQuantityVariance().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				// absolute error measures
				measureName 	= getErrorMeasureName( quantitativeVariable, RELATIVE_ERROR_LABEL , false );
				measureCaption 	= getErrorMeasureCaption( quantitativeVariable, RELATIVE_ERROR_LABEL , TOTAL_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getTotalQuantityRelativeError().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				measureName 	= getErrorMeasureName( quantitativeVariable, ABSOLUTE_ERROR_LABEL , false );
				measureCaption 	= getErrorMeasureCaption( quantitativeVariable, ABSOLUTE_ERROR_LABEL , TOTAL_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getTotalQuantityAbsoluteError().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				measureName 	= getErrorMeasureName( quantitativeVariable, VARIANCE_LABEL , false );
				measureCaption 	= getErrorMeasureCaption( quantitativeVariable, VARIANCE_LABEL , TOTAL_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getTotalQuantityVariance().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
			}
			
		}
		
//		for ( QuantitativeVariable var : entity.getDefaultProcessingChainQuantitativeOutputVariables() ) {
//			Field<BigDecimal> measureField = factTable.getMeasureField(var);
//			Measure measure = new Measure( getRolapSchema(), this, var );
//			measures.put(measure, measureField);
//			// not used now
////			for ( VariableAggregate varAgg : var.getAggregates() ) {
////				Field<BigDecimal> measureField = factTable.getMeasureField(varAgg);
////				Measure measure = new Measure(getRolapSchema(), this, varAgg);
////				measures.put(measure, measureField);
////			}
//		}
		
		if( entity.isSamplingUnit() ) {
			Field<BigDecimal> weightField = factTable.getWeightField();
			Measure measure = new Measure( getRolapSchema(), this, weightField.getName() , "Area" , weightField.getName(), Measure.AGGREGATE_FUNCTION.SUM );
			measures.put( measure, weightField );
	
		
			List<ErrorTable> errorTables = factTable.getErrorTables( factTable.getWorkspace().getAreaVariable() );
			if( errorTables.size() > 0 ){
				ErrorTable errorTable = errorTables.get( 0 );
				
				QuantitativeVariable quantitativeVariable = errorTable.getQuantitativeVariable();
				
				String measureName		= getErrorMeasureName( quantitativeVariable, RELATIVE_ERROR_LABEL , false );
				String measureCaption 	= getAreaErrorMeasureCaption( quantitativeVariable, RELATIVE_ERROR_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getAreaRelativeError().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				measureName 	= getErrorMeasureName( quantitativeVariable, ABSOLUTE_ERROR_LABEL , false );
				measureCaption 	= getAreaErrorMeasureCaption( quantitativeVariable, ABSOLUTE_ERROR_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getAreaAbsoluteError().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
				
				measureName 	= getErrorMeasureName( quantitativeVariable,  VARIANCE_LABEL , false );
				measureCaption 	= getAreaErrorMeasureCaption( quantitativeVariable, VARIANCE_LABEL );
				measure = new Measure( getRolapSchema(), this, measureName , measureCaption, errorTable.getAreaVariance().getName(), AGGREGATE_FUNCTION.SUM );
				this.errorMeasures.add( measure );
			}
		
		
		}

	}

	private String getErrorMeasureName( QuantitativeVariable quantitativeVariable , String errorType , boolean meanValue ){
		
		String name = Measure.getName( quantitativeVariable );
		if( meanValue ){
			name += " HA";
		}
		
		String string = String.format( errorColumnNameFormat, name , errorType );
		string = StringUtils.capitalize( string );
		return string;
	}
	
	private String getErrorMeasureCaption( QuantitativeVariable quantitativeVariable , String errorType , String variableType ){
		
		String name = Measure.getName( quantitativeVariable );
		
		String string = String.format( errorColumnCaptionFormat, name , variableType , errorType );
		string = StringUtils.capitalize( string );
		return string;
	}
	
	private String getAreaErrorMeasureCaption( QuantitativeVariable quantitativeVariable , String errorType ){
		
		String name = Measure.getName( quantitativeVariable );
		
		String string = String.format( areaErrorColumnCaptionFormat, name , errorType );
		string = StringUtils.capitalize( string );
		return string;
	}

	private void createAggNames() {
		this.aggNames = new ArrayList<AggName>();
		
		for (AoiAggregateTable aggTable : factTable.getAoiAggregateTables()) {
			AggName aggName = new AggName( aggTable );
			this.aggNames.add( aggName );
		}
		
		for ( ErrorTable errorTable : factTable.getErrorTables() ){
			AggName aggName = new AggErrorName( errorTable );
			this.aggNames.add( aggName );
		}
		
	//	if ( factTable.isGeoreferenced() ) {
	//		Collection<AggregateTable> aggregateTables = factTable.getAggregateTables();
	//		for ( AggregateTable aggregateTable : aggregateTables ) {
	//			AggName aggName = new AggName(aggregateTable);
	//			this.aggNames.add(aggName);
	//		}
	//	}
	}
	
	public Field<Integer> getStratumField() {
		return factTable.getStratumField();
	}

	public StratumDimension getStratumDimension() {
		return rolapSchema.getStratumDimension();
	}

	public Map<Dimension, Field<Integer>> getDimensionUsages() {
		return CollectionUtils.unmodifiableMap(dimensionUsages);
	}

	public Map<AoiDimension, Field<Integer>> getAoiDimensionUsages() {
		return CollectionUtils.unmodifiableMap(aoiDimensionUsages);
	}

	public Map<Measure, Field<BigDecimal>> getMeasures() {
		return CollectionUtils.unmodifiableMap( measures );
	}
	
	public List<Measure> getMeasuresOrdered() {
		List<Measure> list = new ArrayList<Measure>( measures.keySet() );
		list.sort( new Comparator<Measure>() {
			@Override
			public int compare(Measure o1, Measure o2) {
				return o1.getName().compareTo( o2.getName() );
			}
		});
		return CollectionUtils.unmodifiableList( list );
	}

	public List<Measure> getErrorMeasures() {
		return CollectionUtils.unmodifiableList( errorMeasures );
	}
	
	public RolapSchema getRolapSchema() {
		return rolapSchema;
	}

	public DataTable getFactTable() {
		return factTable;
	}

	public String getName() {
		return name;
	}

	public String getTable() {
		return table;
	}

	public String getSchema() {
		return schema;
	}

	public List<AggName> getAggNames() {
		return aggNames;
	}
	

	public class AggName {

		protected List<AggForeignKey> aggForeignKeys;
		protected List<AggMeasure> aggMeasures;
		protected List<AggLevel> aggLevels;

		private AoiAggregateTable aggregateTable;

		private AggName() {
			aggForeignKeys = new ArrayList<AggForeignKey>();
			aggMeasures = new ArrayList<AggMeasure>();
			aggLevels = new ArrayList<AggLevel>();
		}
		
		private AggName(AoiAggregateTable aggregateTable){
			this();
			this.aggregateTable = aggregateTable;

			createAggForeignKeys();
			createAggMeasures();
			createAggLevels();
		}

		public String getName() {
			return aggregateTable.getName();
		}

		public Field<Integer> getFactCountField() {
			return aggregateTable.getAggregateFactCountField();
		}

		private void createAggForeignKeys() {
			Map<Dimension, Field<Integer>> dimUsages = Cube.this.getDimensionUsages();
			for ( Entry<Dimension, Field<Integer>> entry : dimUsages.entrySet() ) {
				Field<Integer> field = entry.getValue();
				AggForeignKey aggFK = new AggForeignKey(field.getName(), field.getName());
				aggForeignKeys.add(aggFK);
			}
			
			Field<Integer> stratumField = Cube.this.getStratumField();
			if ( stratumField != null ) {
				AggForeignKey aggFK = new AggForeignKey( stratumField.getName(), stratumField.getName() );
				aggForeignKeys.add(aggFK);
			}
			
		}

		private void createAggMeasures() {
			Map<Measure, Field<BigDecimal>> measures = Cube.this.getMeasures();
			for ( Entry<Measure, Field<BigDecimal>> entry : measures.entrySet() ) {
				Measure measure = entry.getKey();
//				Field<BigDecimal> field = entry.getValue();
				AggMeasure aggMeasure = new AggMeasure(measure.getColumn(), measure.getName());
				aggMeasures.add(aggMeasure);
			}
		}

		private void createAggLevels() {
			AoiLevel aggTableLevel = this.aggregateTable.getAoiLevel();
			AoiHierarchy aoiHierarchy = this.aggregateTable.getAoiHierarchy();
			
			AoiDimension aoiDim = getAoiDimension(aoiHierarchy);
			for ( AoiLevel level : aoiHierarchy.getLevels() ) {
				if ( level.getRank() <= aggTableLevel.getRank() ) {
					Field<Integer> field = aggregateTable.getAoiIdField(level);
					AggLevel aggLevel = new AggLevel(aoiDim.getHierarchy().getName(), level.getName(), field.getName());
					aggLevels.add(aggLevel);
				}
			}
		}

		public List<AggForeignKey> getAggForeignKeys() {
			return aggForeignKeys;
		}

		public List<AggMeasure> getAggMeasures() {
			return aggMeasures;
		}

		public List<AggLevel> getAggLevels() {
			return aggLevels;
		}
	}
	
	public class AggErrorName extends AggName{

		private ErrorTable errorTable;

		private AggErrorName( ErrorTable errorTable ) {
			super();
			this.errorTable = errorTable;
			
			createAggForeignKeys();
			createAggMeasures();
			createAggLevels();
		}
		
		public String getName() {
			return errorTable.getName();
		}

//		public Field<Integer> getFactCountField() {
//			return aggregateTable.getAggregateFactCountField();
//		}
		
		private void createAggMeasures() {
			QuantitativeVariable quantitativeVariable = errorTable.getQuantitativeVariable();
			
			if( quantitativeVariable.getId() == -1 ){
				
				String measureName 		= getErrorMeasureName( quantitativeVariable, RELATIVE_ERROR_LABEL , false );
				AggMeasure aggMeasure 	= new AggMeasure( errorTable.getAreaRelativeError().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				measureName = getErrorMeasureName( quantitativeVariable, ABSOLUTE_ERROR_LABEL , false );
				aggMeasure 	= new AggMeasure( errorTable.getAreaAbsoluteError().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				measureName = getErrorMeasureName( quantitativeVariable,  VARIANCE_LABEL , false );
				aggMeasure 	= new AggMeasure( errorTable.getAreaVariance().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
			} else {
				
				String measureName 		= getErrorMeasureName( quantitativeVariable, RELATIVE_ERROR_LABEL , true );
				AggMeasure aggMeasure 	= new AggMeasure( errorTable.getMeanQuantityRelativeError().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				measureName = getErrorMeasureName( quantitativeVariable, ABSOLUTE_ERROR_LABEL , true );
				aggMeasure 	= new AggMeasure( errorTable.getMeanQuantityAbsoluteError().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				measureName = getErrorMeasureName( quantitativeVariable,  VARIANCE_LABEL , true );
				aggMeasure 	= new AggMeasure( errorTable.getMeanQuantityVariance().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				// absolute error measures
				measureName = getErrorMeasureName( quantitativeVariable, RELATIVE_ERROR_LABEL , false );
				aggMeasure 	= new AggMeasure( errorTable.getTotalQuantityRelativeError().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				measureName = getErrorMeasureName( quantitativeVariable, ABSOLUTE_ERROR_LABEL , false );
				aggMeasure 	= new AggMeasure( errorTable.getTotalQuantityAbsoluteError().getName() , measureName );
				aggMeasures.add(aggMeasure);
				
				measureName = getErrorMeasureName( quantitativeVariable,  VARIANCE_LABEL , false );
				aggMeasure 	= new AggMeasure( errorTable.getTotalQuantityVariance().getName() , measureName );
				aggMeasures.add(aggMeasure);
			}
			
		}

		private void createAggForeignKeys() {
			String aggFKColumn = errorTable.getCategoryIdField().getName();
			AggForeignKey aggFK = new AggForeignKey(aggFKColumn , aggFKColumn);
			aggForeignKeys.add(aggFK);
		}
		
		private void createAggLevels() {
//			Aoi aoi 					= errorTable.getAoi();
			AoiLevel aggTableLevel		= errorTable.getAoiLevel();
			AoiHierarchy aoiHierarchy 	= aggTableLevel.getHierarchy();
			AoiDimension aoiDim 		= getAoiDimension( aoiHierarchy );
//			Field<Integer> aoiField 	= errorTable.getAoiField();
			
//			AggLevel aggLevel = new AggLevel( aoiDim.getHierarchy().getName() , aoiLevel.getName() , aoiField.getName() );
//			aggLevels.add(aggLevel);
//			
			
//			AoiDimension aoiDim = getAoiDimension(aoiHierarchy);
			for ( AoiLevel level : aoiHierarchy.getLevels() ) {
				if ( level.getRank() <= aggTableLevel.getRank() ) {
					Field<Integer> field = errorTable.getAoiIdField( level );
					AggLevel aggLevel = new AggLevel(aoiDim.getHierarchy().getName(), level.getName(), field.getName());
					aggLevels.add(aggLevel);
				}
			}
		}
	}
	

	public class AggForeignKey {
		private String factColumn;
		private String aggColumn;

		AggForeignKey(String factColumn, String aggColumn) {
			this.factColumn = factColumn;
			this.aggColumn = aggColumn;
		}

		public String getFactColumn() {
			return factColumn;
		}

		public String getAggColumn() {
			return aggColumn;
		}
	}

	public class AggMeasure {
		private String column;
		private String name;

		AggMeasure(String column, String name) {
			this.column = column;
			this.name = name;
		}

		public String getColumn() {
			return column;
		}

		public String getName() {
			return name;
		}
	}

	public class AggLevel {
		private String column;
		private String name;
		private String hierarchy;

		AggLevel(String hierarchy, String name, String column) {
			this.hierarchy = hierarchy;
			this.name = name;
			this.column = column;
		}

		public String getHierarchy() {
			return hierarchy;
		}

		public String getColumn() {
			return column;
		}

		public String getName() {
			return name;
		}
	}
}
