package org.openforis.calc.persistence.postgis;

import org.jooq.Schema;

/**
 * 
 * @author G. Miceli
 *
 */
public class DropSchemaStep extends DdlStep {

	DropSchemaStep(Psql psql, boolean ifExists, Schema schema) {
		super(psql);
		append("drop schema ");
		if ( ifExists ) {
			append("if exists ");
		}
		append(schema);
	}

	public CascadeStep cascade() {
		return new CascadeStep();
	}

	public class CascadeStep extends DdlStep {

		CascadeStep() {
			super(DropSchemaStep.this);
			append(" cascade");
		}
	}
}
