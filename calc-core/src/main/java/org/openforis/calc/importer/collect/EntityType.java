package org.openforis.calc.importer.collect;

/**
 * @author G. Miceli
 */
public enum EntityType {
	CLUSTER, PLOT, SPECIMEN, INTERVIEW;
	
	public boolean isObservationUnit() {
		return this == PLOT || this == SPECIMEN || this == INTERVIEW;
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
