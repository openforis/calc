package org.openforis.calc.persistence.postgis;

import org.jooq.Select;
import org.jooq.Table;


/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class CreateTableStep extends DdlStep {

	CreateTableStep(Psql psql, Table<?> table) {
		super(psql);
		append("create table ");
		append(table);
	}

	public AsStep as(Select<?> select) {
		return new AsStep(select);
	}
	
	public class AsStep extends DdlStep {
		
		AsStep(Select<?> select) {
			super(CreateTableStep.this);
			append("as ");
			append(select);
		}
		
	}
	
}
