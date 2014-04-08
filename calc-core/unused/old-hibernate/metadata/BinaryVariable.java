package org.openforis.calc.metadata;


/**
 * A special type of CategoricalVariable which may take only two values (TRUE or FALSE) plus NA.
 * 
 * "Binary Variable" is the statistical data type analogous to "boolean" in computer science and
 * algebra. 
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public class BinaryVariable extends CategoricalVariable<Boolean> {

	private static final long serialVersionUID = 1L;
	
//	@Column(name = "default_value")
	private Boolean defaultValue;

	// TODO implement as subclass of CategoricalVariable?
	public BinaryVariable() {
		super(Scale.BINARY);
	}
	
	@Override
	public void setScale(Scale scale) {
		if ( scale != Scale.BINARY ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
	}
	
	@Override
	public Type getType() {
		return Type.BINARY;
	}

	@Override
	public Boolean getDefaultValueTemp() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}
}