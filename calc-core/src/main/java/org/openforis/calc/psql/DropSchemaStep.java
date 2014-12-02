package org.openforis.calc.psql;

import org.jooq.Schema;

/**
 * 
 * @author G. Miceli
 *
 */
public class DropSchemaStep extends ExecutablePsqlPart {

	DropSchemaStep(Psql psql, boolean ifExists, Schema schema, boolean cascade) {
		super(psql);
		append("drop schema ");
		if ( ifExists ) {
			append("if exists ");
		}
		append(schema);
		if( cascade ) {
			append(" cascade");
		}
	}
	
	DropSchemaStep(Psql psql, boolean ifExists, Schema schema) {
		this( psql, ifExists , schema , false );
	}

	public CascadeStep cascade() {
		return new CascadeStep();
	}

	public class CascadeStep extends ExecutablePsqlPart {

		CascadeStep() {
			super(DropSchemaStep.this);
			append(" cascade");
		}
	}
}
