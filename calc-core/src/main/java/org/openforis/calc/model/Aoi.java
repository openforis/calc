package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class Aoi extends org.openforis.calc.persistence.jooq.tables.pojos.Aoi implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getAoiId();
	}

	@Override
	public void setId(Integer id) {
		super.setAoiId(id);
	}

}
