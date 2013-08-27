package org.openforis.calc.psql;

import org.jooq.Schema;

/**
 * 
 * @author G. Miceli
 *
 */
public class CreateSchemaStep extends ExecutablePsqlPart {
	CreateSchemaStep(Psql psql, Schema schema) {
		super(psql);
		append("create schema ");
		append(schema);
	}
}
