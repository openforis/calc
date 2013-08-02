package org.openforis.calc.metadata;

import javax.persistence.DiscriminatorValue;

/**
 * A special type of CategoricalVariable which may take only two values (TRUE or FALSE) plus NA.
 * 
 * "Binary Variable" is the statistical data type analogous to "boolean" in computer science and
 * algebra. 
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@DiscriminatorValue("B")
public class BinaryVariable extends CategoricalVariable {
	
	// TODO implement as subclass of CategoricalVariable?
	public BinaryVariable() {
		super(Scale.BINARY);
		super.setMultipleResponse(false);
	}
	
	@Override
	public void setScale(Scale scale) {
		if ( scale != Scale.BINARY ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
	}

	@Override
	public void setMultipleResponse(boolean multipleResponse) {
		if ( multipleResponse ) {
			throw new IllegalArgumentException("Binary variables may not be multiple response");
		}
	}
	
	@Override
	public Type getType() {
		return Type.BINARY;
	}
	
	@Override
	public boolean isOrdered() {
		return true;
	}
}