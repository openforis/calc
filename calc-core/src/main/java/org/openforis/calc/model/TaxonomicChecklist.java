package org.openforis.calc.model;

/**
 * 
 * @author M. Togna
 * 
 */
public class TaxonomicChecklist extends org.openforis.calc.persistence.jooq.tables.pojos.TaxonomicChecklist implements Identifiable {

	private static final long serialVersionUID = 1L;

	public TaxonomicChecklist() {
	}

	@Override
	public Integer getId() {
		return super.getChecklistId();
	}

	@Override
	public void setId(Integer id) {
		super.setChecklistId(id);
	}

}
