package org.openforis.calc.schema;

import org.openforis.calc.metadata.VariableAggregate;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * @author M. Togna
 */
public class Measure extends Member {

	private Cube cube;
	private VariableAggregate aggregate;

	private String aggregator;
	private String name;
	private String caption;

	Measure(RolapSchema rolapSchema, Cube cube, VariableAggregate aggregate) {
		super(rolapSchema);
		this.cube = cube;
		this.aggregate = aggregate;

		this.name = aggregate.getName();
		this.caption = aggregate.getCaption();
		this.aggregator = aggregate.getAggregateFunction();
	}

	public Cube getCube() {
		return cube;
	}

	public VariableAggregate getAggregate() {
		return aggregate;
	}

	public String getAggregator() {
		return aggregator;
	}

	public String getName() {
		return name;
	}

	public String getCaption() {
		return caption;
	}

}
