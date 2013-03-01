package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public class ClusterDimensionTable extends DimensionTable {
	private static final long serialVersionUID = 1L;

	private static final String TABLE_NAME = "cluster";
    
    ClusterDimensionTable(String schema) {
		super(schema, TABLE_NAME);
	}
}
