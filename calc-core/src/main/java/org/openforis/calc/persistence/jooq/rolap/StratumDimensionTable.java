package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public class StratumDimensionTable extends DimensionTable<StratumDimensionRecord> {
	private static final long serialVersionUID = 1L;

	private static final String TABLE_NAME = "stratum";
    
    StratumDimensionTable(String schema) {
		super(schema, TABLE_NAME, StratumDimensionRecord.class);
	}
}
