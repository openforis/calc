package org.openforis.calc.schema;

import org.openforis.calc.metadata.VariableAggregate;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class Measure extends Member {

	private Cube cube;
	private VariableAggregate aggregate;

	public Measure(RolapSchema rolapSchema, Cube cube, VariableAggregate aggregate) {
		super(rolapSchema);
		this.cube = cube;
		this.aggregate = aggregate;
	}
	
	public Cube getCube() {
		return cube;
	}
	
	public VariableAggregate getAggregate() {
		return aggregate;
	}
}
