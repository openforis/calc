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
	
	public AuthorizationStep authorization(String user) {
		return new AuthorizationStep(user);
	}
	
	public class AuthorizationStep extends ExecutablePsqlPart {

		public AuthorizationStep(String user) {
			super(CreateSchemaStep.this);
			append("AUTHORIZATION ");
			append("\"");
			append(user);
			append("\"");
		}
	}
}
