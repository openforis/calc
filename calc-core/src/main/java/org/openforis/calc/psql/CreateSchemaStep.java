package org.openforis.calc.psql;

import org.jooq.Schema;

/**
 * 
 * @author G. Miceli
 *
 */
public class CreateSchemaStep extends PsqlPart {
	CreateSchemaStep(Psql psql, Schema schema) {
		super(psql);
		append("create schema ");
		append(schema);
	}
}
