/**
 * 
 */
package org.openforis.calc.persistence.jooq.tables.records;

import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.calc.persistence.jooq.tables.DimensionTable;

/**
 * @author M. Togna
 * 
 */
public class DimensionRecord extends UpdatableRecordImpl<DimensionRecord> {

	private static final long serialVersionUID = 1L;

	private DimensionTable table;

	/**
	 * @param table
	 */
	public DimensionRecord(DimensionTable table) {
		super(table);
		this.table = table;
	}

	public int getId() {
		return getValue(table.ID);
	}

	public void setId(int id) {
		setValue(table.ID, id);
	}

	public String getCode() {
		return getValue(table.CODE);
	}

	public void setCode(String code) {
		setValue(table.CODE, code);
	}

	public String getLabel() {
		return getValue(table.LABEL);
	}

	public void setLabel(String label) {
		setValue(table.LABEL, label);
	}

}
