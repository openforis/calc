package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class AoiDimensionRecord extends HierarchicalDimensionRecord<AoiDimensionRecord> {

	private static final long serialVersionUID = 1L;

	AoiDimensionRecord(AoiDimensionTable table) {
		super(table);
	}
	
	public Integer getParentId() {
		return getValue(((AoiDimensionTable)getTable()).PARENT_ID);
	}

	public void setParentId(Integer id) {
		setValue(((AoiDimensionTable)getTable()).PARENT_ID, id);
	}
}
