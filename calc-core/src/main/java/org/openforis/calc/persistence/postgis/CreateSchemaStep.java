package org.openforis.calc.persistence.postgis;

import org.jooq.Schema;

/**
 * 
 * @author G. Miceli
 *
 */
public class CreateSchemaStep extends DdlStep {
	CreateSchemaStep(Psql psql, Schema schema) {
		super(psql);
		append("create schema ");
		append(schema);
	}
}
