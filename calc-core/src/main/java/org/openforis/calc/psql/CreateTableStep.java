package org.openforis.calc.psql;

import org.jooq.Field;
import org.jooq.Select;
import org.jooq.Table;


/**
 * 
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class CreateTableStep extends ExecutablePsqlPart {
	
	CreateTableStep(Psql psql, Table<?> table) {
		super(psql);
		append("create table ");
		append(table);
	}

	public AsStep as(Select<?> select) {
		return new AsStep(select);
	}
	
	public CreateTableStep columns(Field<?>... fields) {
		append("(");
		for (int i = 0; i < fields.length; i++) {
			Field<?> field = fields[i];
			append(field.getName());
			append(" ");
			append(field.getDataType().getTypeName());
			if ( i < fields.length - 1 ) {
				append(", ");
			}
		}
		append(")");
		return this;
	}
	
	public class AsStep extends ExecutablePsqlPart {
		AsStep(Select<?> select) {
			super(CreateTableStep.this);
			append("as ");
			append(select);
		}
	}
}
