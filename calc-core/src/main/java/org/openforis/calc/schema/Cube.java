package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jooq.Field;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
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

	private RolapSchema rolapSchema;
	private FactTable factTable;

	private String name;
	private String schema;
	private String table;
	private List<AggName> aggNames;

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
	}

	private void createMeasures() {
		measures = new HashMap<Measure, Field<BigDecimal>>();
		Entity entity = factTable.getEntity();

		for ( QuantitativeVariable var : entity.getDefaultProcessingChainQuantitativeOutputVariables() ) {
			Field<BigDecimal> measureField = factTable.getMeasureField(var);
			Measure measure = new Measure( getRolapSchema(), this, var );
			measures.put(measure, measureField);
			// not used now
//			for ( VariableAggregate varAgg : var.getAggregates() ) {
//				Field<BigDecimal> measureField = factTable.getMeasureField(varAgg);
//				Measure measure = new Measure(getRolapSchema(), this, varAgg);
//				measures.put(measure, measureField);
//			}
		}
		
		if( entity.isSamplingUnit() ) {
			Field<BigDecimal> weightField = factTable.getWeightField();
			Measure measure = new Measure( getRolapSchema(), this, weightField.getName() , "Area" , Measure.AGGREGATE_FUNCTION.SUM );
			measures.put( measure, weightField );
		}
	}
	
	private void createAggNames() {
	this.aggNames = new ArrayList<AggName>();
	
	for (AoiAggregateTable aggTable : factTable.getAoiAggregateTables()) {
		AggName aggName = new AggName( aggTable );
		this.aggNames.add(aggName);
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
		return CollectionUtils.unmodifiableMap(measures);
	}

	public RolapSchema getRolapSchema() {
		return rolapSchema;
	}

	public FactTable getFactTable() {
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

		private List<AggForeignKey> aggForeignKeys;
		private List<AggMeasure> aggMeasures;
		private List<AggLevel> aggLevels;

		private AoiAggregateTable aggregateTable;

		public AggName(AoiAggregateTable aggregateTable) {
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
			aggForeignKeys = new ArrayList<AggForeignKey>();

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
			aggMeasures = new ArrayList<AggMeasure>();

			Map<Measure, Field<BigDecimal>> measures = Cube.this.getMeasures();
			for ( Entry<Measure, Field<BigDecimal>> entry : measures.entrySet() ) {
				Measure measure = entry.getKey();
				Field<BigDecimal> field = entry.getValue();
				AggMeasure aggMeasure = new AggMeasure(field.getName(), measure.getName());
				aggMeasures.add(aggMeasure);
			}
		}

		private void createAggLevels() {
			aggLevels = new ArrayList<AggLevel>();
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
