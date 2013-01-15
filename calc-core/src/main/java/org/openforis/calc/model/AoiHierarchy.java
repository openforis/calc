package org.openforis.calc.model;

/**
 * @author Mino Togna
 */
public class AoiHierarchy extends org.openforis.calc.persistence.jooq.tables.pojos.AoiHierarchy implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return getAoiHierarchyId();
	}

	@Override
	public void setId(Integer id) {
		super.setAoiHierarchyId(id);
	}

}
