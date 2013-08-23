package org.openforis.calc.rdb;

import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import org.jooq.Record;
import org.jooq.TableField;

/**
 * 
 * @author G. Miceli
 */
public class StratumDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;

	public final TableField<Record, Integer> ID = copyField(STRATUM.ID); 
	public final TableField<Record, Integer> STRATUM_NO = copyField(STRATUM.STRATUM_NO);
	public final TableField<Record, String> CAPTION = copyField(STRATUM.CAPTION);
	public final TableField<Record, String> DESCRIPTION = copyField(STRATUM.DESCRIPTION);
			
	public StratumDimensionTable(OutputSchema schema) {
		super("_stratum_dim", schema);
	}
}
