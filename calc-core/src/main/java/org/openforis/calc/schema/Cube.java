package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;

/**
 * 
 * @author G. Miceli
 * 
 */
public class Cube {

	private Map<Dimension, Field<Integer>> dimensionUsages;
	private Map<AoiDimension, Field<Integer>> aoiDimensionUsages;
	private List<Measure> measures;

	private RolapSchema rolapSchema;
	private FactTable factTable;

	private String name;
	private String schema;
	private String table;

	Cube(RolapSchema rolapSchema, FactTable factTable) {
		this.name = factTable.getEntity().getName();
		this.schema = rolapSchema.getOutputSchema().getName();
		this.table = factTable.getName();

		// this.dimensionUsages = new ArrayList<Dimension>();
		this.measures = new ArrayList<Measure>();
		this.rolapSchema = rolapSchema;
		this.factTable = factTable;

		createDimensionUsages();
		createAoiDimensionUsages();
	}

	// void addDimensionUsage(Dimension dim) {
	// dimensionUsages.add(dim);
	// }

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

		Map<CategoricalVariable, CategoryDimension> dimensionsMap = rolapSchema.getSharedDimensionsMap();
		for ( CategoricalVariable var : dimensionsMap.keySet() ) {
			Field<Integer> field = factTable.getDimensionIdField(var);
			if ( field != null ) {
				Dimension dim = dimensionsMap.get(var);
				dimensionUsages.put(dim, field);
			}
		}
	}

	void addMeasure(Measure measure) {
		measures.add(measure);
	}

	public Map<Dimension, Field<Integer>> getDimensionUsages() {
		return Collections.unmodifiableMap(dimensionUsages);
	}

	public List<Measure> getMeasures() {
		return Collections.unmodifiableList(measures);
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

	public String getSchema() {
		return schema;
	}

	public String getTable() {
		return table;
	}

	public Map<AoiDimension, Field<Integer>> getAoiDimensionUsages() {
		return Collections.unmodifiableMap(aoiDimensionUsages);
	}

}
