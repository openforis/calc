package org.openforis.calc.metadata;

import javax.persistence.DiscriminatorValue;

/**
 * A special type of categorical variable which may take one of three values; TRUE, FALSE or NA.
 * 
 * "Binary Variable" is the statistical data type analogous to "boolean" in computer science and
 * algebra. 
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("B")
public class BinaryVariable extends Variable {
	
	public BinaryVariable() {
		setScale(Scale.BINARY);
	}
	
	@Override
	public void setScale(Scale scale) {
		//TODO implement for CategoricalVariable and QuantitativeVariable
		if ( scale != Scale.BINARY ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}

	@Override
	public Type getType() {
		return Type.BINARY;
	}
}