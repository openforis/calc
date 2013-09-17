package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;

/**
 * A variable which may take on a single text value (it can be used as degenerate dimension).
 * 
 * @author M. Togna
 * @author S. Ricci
 */
@javax.persistence.Entity
@DiscriminatorValue("T")
public class TextVariable extends Variable<String> {

	@Column(name = "default_value")
	private String defaultValue;

	@Override
	public Type getType() {
		return Type.TEXT;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public void setScale(Scale scale) {
		if ( scale != Scale.TEXT ) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}
	
}
