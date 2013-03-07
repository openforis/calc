package org.openforis.calc.model;


public enum MeasurementScale {
	RATIO,
	INTERVAL,
	NOMINAL,
	ORDINAL,
	OTHER;

	public boolean isAllowed(AggregateType fn) {
		switch (fn) {
		case SUM:
		case PROPORTION:
			return this == RATIO;
		case FREQUENCY:
			return this==NOMINAL || this==ORDINAL;
		default:
			return this == RATIO || this == INTERVAL || this == OTHER; 
		}
	}
}