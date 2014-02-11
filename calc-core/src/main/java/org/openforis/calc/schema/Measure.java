package org.openforis.calc.schema;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.metadata.QuantitativeVariable;
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

	Measure( RolapSchema rolapSchema, Cube cube, QuantitativeVariable variable ) {
		this( rolapSchema, cube, variable.getName(), variable.getCaption(), VariableAggregate.AGGREGATE_TYPE.SUM.toString() );
	}
	
	Measure(RolapSchema rolapSchema, Cube cube, String name, String caption, String aggregator) {
		super( rolapSchema );
		
		this.cube = cube;
		this.name = capitalize( name );
		this.caption = ( StringUtils.isBlank(caption) ) ? this.name : capitalize( caption );
		this.aggregator = aggregator;
	}

	public Cube getCube() {
		return cube;
	}

	@Deprecated
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
