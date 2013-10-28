package org.openforis.calc.psql;

import org.jooq.Select;
import org.jooq.Table;


/**
 * 
 * @author S. Ricci
 *
 */
public class CreateViewStep extends PsqlPart {

	CreateViewStep(Psql psql, Table<?> view) {
		super(psql);
		append("create view ");
		append(view);
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
