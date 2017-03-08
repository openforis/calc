package org.openforis.calc.metadata;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A variable which may take on a date value
 * 
 * @author M. Togna
 */
public class DateVariable extends Variable<LocalDate> {

	private static final long	serialVersionUID	= 1L;

	// @Column(name = "default_value")
	private LocalDate			defaultValue;

	@Override
	public Type getType() {
		return Type.DATE;
	}

	@Override
	@JsonIgnore
	public LocalDate getDefaultValueTemp() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(LocalDate defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public void setScale(Scale scale) {
		if (scale != Scale.DATE) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}

}
