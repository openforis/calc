package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class Cluster extends org.openforis.calc.persistence.jooq.tables.pojos.Cluster implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getClusterId();
	}

	@Override
	public void setId(Integer id) {
		super.setClusterId(id);
	}

}
