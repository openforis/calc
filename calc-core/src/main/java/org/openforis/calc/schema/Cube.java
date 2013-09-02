package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.VariableAggregate;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class Cube {

	private Map<Dimension, Field<Integer>> dimensionUsages;
	private Map<AoiDimension, Field<Integer>> aoiDimensionUsages;
	private Map<Measure, Field<BigDecimal>> measures;

	private RolapSchema rolapSchema;
	private FactTable factTable;

	private String name;

	private Table table;

	// private String schema;
	// private String table;

	Cube(RolapSchema rolapSchema, FactTable factTable) {
		this.name = factTable.getEntity().getName();

		// this.dimensionUsages = new ArrayList<Dimension>();
		this.rolapSchema = rolapSchema;
		this.factTable = factTable;

		createDimensionUsages();
		createAoiDimensionUsages();
		createMeasures();
		createTable();
	}

	private void createTable() {
		OutputSchema outputSchema = rolapSchema.getOutputSchema();
		this.table = new Table(factTable.getName(), outputSchema.getName());
	}

	private void createMeasures() {
		measures = new HashMap<Measure, Field<BigDecimal>>();
		Entity entity = factTable.getEntity();

		for ( QuantitativeVariable var : entity.getQuantitativeVariables() ) {
			for ( VariableAggregate varAgg : var.getAggregates() ) {
				Field<BigDecimal> measureField = factTable.getMeasureField(varAgg);
				Measure measure = new Measure(getRolapSchema(), this, varAgg);
				measures.put(measure, measureField);
			}
		}
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

	public Field<Integer> getStratumIdField() {
		return factTable.getStratumIdField();
	}

	public StratumDimension getStratumDimension() {
		return rolapSchema.getStratumDimension();
	}

	public Map<Dimension, Field<Integer>> getDimensionUsages() {
		return Collections.unmodifiableMap(dimensionUsages);
	}

	public Map<AoiDimension, Field<Integer>> getAoiDimensionUsages() {
		return Collections.unmodifiableMap(aoiDimensionUsages);
	}

	public Map<Measure, Field<BigDecimal>> getMeasures() {
		return Collections.unmodifiableMap(measures);
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

	public Table getTable() {
		return table;
	}

}
