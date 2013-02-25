package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class Category extends org.openforis.calc.persistence.jooq.tables.pojos.Category implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getCategoryId();
	}

	@Override
	public void setId(Integer id) {
		super.setCategoryId(id);
	}
}
