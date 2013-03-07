package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 * TODO migrate to IDM?
 */
public enum VariableType {
	RATIO(MeasurementScale.RATIO),
	INTERVAL(MeasurementScale.RATIO),
	ORDINAL(MeasurementScale.ORDINAL),
	PROPORTION(MeasurementScale.OTHER),
	NOMINAL(MeasurementScale.NOMINAL),
	BOOLEAN(MeasurementScale.NOMINAL),
	MULTIPLE_RESPONSE(MeasurementScale.NOMINAL);

	private MeasurementScale scale;
	
	VariableType(MeasurementScale scale) {
		this.scale = scale;
	}

	public MeasurementScale getScale() {
		return scale;
	}
	
	public boolean isMultiple() {
		return this == MULTIPLE_RESPONSE;
	}

	@Override
	public String toString() {
		return toString().toLowerCase();
	}
	
	public static VariableType get(String name) {
		return VariableType.valueOf(name.toUpperCase());
	}
}