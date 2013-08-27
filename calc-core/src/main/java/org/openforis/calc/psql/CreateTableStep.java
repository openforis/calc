package org.openforis.calc.psql;

import org.jooq.Select;
import org.jooq.Table;


/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class CreateTableStep extends PsqlPart {
	CreateTableStep(Psql psql, Table<?> table) {
		super(psql);
		append("create table ");
		append(table);
	}

	public AsStep as(Select<?> select) {
		return new AsStep(select);
	}
	
	public class AsStep extends PsqlPart {
		AsStep(Select<?> select) {
			super(CreateTableStep.this);
			append("as ");
			append(select);
		}
	}
}
