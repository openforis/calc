package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class InterviewFactTable extends FactTable {
	private static final long serialVersionUID = 1L;
	
	InterviewFactTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getFactTableName(), unit);
		initFields();
	}

	protected void initFields() {
		initUserDefinedFields();
	}
}
