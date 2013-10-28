package org.openforis.calc.psql;

import org.jooq.Table;

/**
 * 
 * @author G. Miceli
 *
 */
public class DropViewStep extends ExecutablePsqlPart {

	DropViewStep(Psql psql, boolean ifExists, Table<?> table) {
		super(psql);
		append("drop view ");
		if ( ifExists ) {
			append("if exists ");
		}
		append(table);
	}

	public CascadeStep cascade() {
		return new CascadeStep();
	}
	
	public class CascadeStep extends ExecutablePsqlPart {

		CascadeStep() {
			super(DropViewStep.this);
			append(" cascade");
		}
	}
}
