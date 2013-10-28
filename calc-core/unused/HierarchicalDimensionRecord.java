package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class HierarchicalDimensionRecord<R extends HierarchicalDimensionRecord<R>> extends DimensionRecord<R> {

	private static final long serialVersionUID = 1L;

	HierarchicalDimensionRecord(HierarchicalDimensionTable table) {
		super(table);
	}
	
	public Integer getParentId() {
		return getValue(((HierarchicalDimensionTable)getTable()).PARENT_ID);
	}

	public void setParentId(Integer id) {
		setValue(((HierarchicalDimensionTable)getTable()).PARENT_ID, id);
	}
}
