package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public class Stratum extends org.openforis.calc.persistence.jooq.tables.pojos.Stratum implements Identifiable {

	private static final long serialVersionUID = 1L;

	@Override
	public Integer getId() {
		return super.getStratumId();
	}

	@Override
	public void setId(Integer id) {
		super.setStratumId(id);
	}

}
