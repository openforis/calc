package org.openforis.calc.persistence.jooq.rolap;

import static org.jooq.impl.SQLDataType.*;

import org.jooq.Record;
/**
 * 
 * @author G. Miceli
 *
 */
public abstract class HierarchicalDimensionTable extends DimensionTable {
	private static final long serialVersionUID = 1L;
	
	public final org.jooq.TableField<Record, Integer> PARENT_ID = createField("parent_id", INTEGER, this);

	HierarchicalDimensionTable(String schema, String name) {
		super(schema, name);
	}

}
