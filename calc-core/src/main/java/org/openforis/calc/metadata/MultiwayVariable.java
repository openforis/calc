package org.openforis.calc.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A {@link CategoricalVariable} that is not a {@link BinaryVariable}
 * 
 * @author G. Miceli
 * 
 */
public class MultiwayVariable extends CategoricalVariable<String> {

	private static final long serialVersionUID = 1L;
	private String defaultValue;

	@Override
	public Type getType() {
		return Type.CATEGORICAL;
	}

	@Override
	public boolean isInput() {
		return super.isInput() || getInputCategoryIdColumn() != null;
	}

	/**
	 * Indicates if the order of the categories has meaning (ORDINAL, e.g. severity, size) or if they are unordered categorizations (NOMINAL, e.g. land use, ownership).
	 * 
	 * @return
	 */
	@JsonIgnore
//	public boolean isOrdered() {
//		return getScale() == Scale.ORDINAL;
//	}

	@Override
	public void setScale(Scale scale) {
		if (scale != Scale.NOMINAL && scale != Scale.ORDINAL) {
			throw new IllegalArgumentException("Illegal scale: " + scale);
		}
		super.setScale(scale);
	}

	@Override
	@JsonIgnore
	public String getDefaultValueTemp() {
		return defaultValue;
	}

	@Override
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@JsonIgnore
	public boolean isSpecieCategory(){
		return Boolean.TRUE.equals( super.getSpecieCategory() );
	}
	
}
