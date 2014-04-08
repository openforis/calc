package org.openforis.calc.metadata;

/**
 * Represents a unit of measurement. {@link QuantitativeVariable} 
 * references Unit to allow for automatic unit conversion. 
 *
 * TODO to be developed further
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Unit<T extends Dimension> {
	private String abbreviation;
	private Dimension dimension;
	
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public Dimension getDimension() {
		return dimension;
	}
}