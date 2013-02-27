/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import org.jooq.impl.UpdatableRecordImpl;

/**
 * @author M. Togna
 * 
 */
public abstract class DimensionRecord<R extends DimensionRecord<R>> extends UpdatableRecordImpl<R> {

	private static final long serialVersionUID = 1L;

	private DimensionTable<?> table;

	/**
	 * @param table
	 */
	public DimensionRecord(DimensionTable<R> table) {
		super(table);
		this.table = table;
	}

	public Integer getId() {
		return getValue(table.ID);
	}

	public void setId(Integer id) {
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
