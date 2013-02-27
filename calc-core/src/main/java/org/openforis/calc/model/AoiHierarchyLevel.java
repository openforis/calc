package org.openforis.calc.model;

/**
 * @author M. Togna
 */
public class AoiHierarchyLevel extends org.openforis.calc.persistence.jooq.tables.pojos.AoiHierarchyLevel implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return getAoiHierarchyLevelId();
	}

	@Override
	public void setId(Integer id) {
		super.setAoiHierarchyLevelId(id);
	}
}
