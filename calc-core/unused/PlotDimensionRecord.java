package org.openforis.calc.persistence.jooq.rolap;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class PlotDimensionRecord extends HierarchicalDimensionRecord<PlotDimensionRecord> {

	private static final long serialVersionUID = 1L;

	PlotDimensionRecord(PlotDimensionTable table) {
		super(table);
	}
	
	public Integer getParentId() {
		return getValue(((PlotDimensionTable)getTable()).PARENT_ID);
	}

	public void setParentId(Integer id) {
		setValue(((PlotDimensionTable)getTable()).PARENT_ID, id);
	}
}
