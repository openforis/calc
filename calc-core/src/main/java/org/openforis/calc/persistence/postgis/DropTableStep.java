package org.openforis.calc.persistence.postgis;

import org.jooq.Schema;
import org.jooq.Table;

/**
 * 
 * @author G. Miceli
 *
 */
public class DropTableStep extends PsqlPart {

	DropTableStep(Psql psql, boolean ifExists, Table<?> table) {
		super(psql);
		append("drop table ");
		if ( ifExists ) {
			append("if exists ");
		}
		append(table);
	}

	public CascadeStep cascade() {
		return new CascadeStep();
	}

	public class CascadeStep extends PsqlPart {

		CascadeStep() {
			super(DropTableStep.this);
			append(" cascade");
		}
	}
}
