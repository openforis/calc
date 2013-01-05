package org.openforis.calc.dataimport.collect;


public enum EntityType {
	CLUSTER, PLOT, SPECIMEN;
	
	public boolean isObservationUnit() {
		return this == PLOT || this == SPECIMEN;
	}

	public boolean isCluster() {
		return this == CLUSTER;
	}

	public static EntityType get(String type) throws InvalidMetadataException {
		try {
			return EntityType.valueOf(type.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new InvalidMetadataException("Invalid entity type '"+type+"'");
		}
	}
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
