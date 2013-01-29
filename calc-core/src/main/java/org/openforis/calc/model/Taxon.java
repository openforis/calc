package org.openforis.calc.model;

/**
 * 
 * @author Mino Togna
 * 
 */
public class Taxon extends org.openforis.calc.persistence.jooq.tables.pojos.Taxon implements Identifiable {

	private static final long serialVersionUID = 1L;

	public static final String TAXON_CODE_COLUMN_NAME = org.openforis.calc.persistence.jooq.tables.Taxon.TAXON.TAXON_CODE.getName();
	
	public Taxon() {
	}

	@Override
	public Integer getId() {
		return super.getTaxonId();
	}

	@Override
	public void setId(Integer id) {
		super.setTaxonId(id);
	}

}
