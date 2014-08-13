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
		this( rolapSchema, cube, variable.getName(), variable.getCaption(), AGGREGATE_FUNCTION.SUM );
	}
	
	Measure( RolapSchema rolapSchema, Cube cube, QuantitativeVariable variable, AGGREGATE_FUNCTION aggregateFunction ) {
		this( rolapSchema, cube, variable.getName(), variable.getCaption(), aggregateFunction );
	}
	
	Measure(RolapSchema rolapSchema, Cube cube, String name, String caption, AGGREGATE_FUNCTION aggregateFunction) {
		super( rolapSchema );
		
		this.cube = cube;
		this.name = capitalize( name );
		this.caption = ( StringUtils.isBlank(caption) ) ? this.name : capitalize( caption );
		this.aggregator = aggregateFunction.toString();
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

	public static enum AGGREGATE_FUNCTION {
		MIN, MAX, SUM, AVG, COUNT, DISTINCT_COUNT;

		public boolean equals(String other) {
			return this.toString().toLowerCase().equals(other);
		}

		public String toString() {
			return super.toString().toLowerCase().replaceAll("_", "-");
		}

		public static boolean isValid(String agg) {
			agg = agg.toLowerCase();
			for( AGGREGATE_FUNCTION aggType : AGGREGATE_FUNCTION.values()) {
				if( aggType.equals(agg) ) {
					return true;
				}
			}
			return false;
		}
	}

}
