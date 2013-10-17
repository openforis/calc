package org.openforis.calc.psql;

import org.jooq.Select;


/**
 * 
 * @author S. Ricci
 *
 */
public class CreateViewStep extends PsqlPart {

	CreateViewStep(Psql psql, String schemaName, String name) {
		super(psql);
		append("create view ");
		append(schemaName);
		append(".");
		append(name);
	}

	public AsStep as(Select<?> select) {
		return new AsStep(select);
	}
	
	public class AsStep extends ExecutablePsqlPart {
		AsStep(Select<?> select) {
			super(CreateViewStep.this);
			append("as ");
			append(select);
		}
	}
}
