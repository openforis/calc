package org.openforis.calc.rolap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author G. Miceli
 *
 */
public class Cube {
	private List<Dimension> dimensionUsages;
	private List<Measure> measures;
	private RolapSchema rolapSchema;
	
	Cube(RolapSchema rolapSchema) {
		this.dimensionUsages = new ArrayList<Dimension>();
		this.measures = new ArrayList<Measure>();
		this.rolapSchema = rolapSchema;
	}
	
	void addDimensionUsage(Dimension dim) {
		dimensionUsages.add(dim);
	}
	
	void addMeasure(Measure measure) {
		measures.add(measure);
	}
	
	public List<Dimension> getDimensionUsages() {
		return Collections.unmodifiableList(dimensionUsages);
	}
	
	public List<Measure> getMeasures() {
		return Collections.unmodifiableList(measures);
	}
	
	public RolapSchema getRolapSchema() {
		return rolapSchema;
	}
}
