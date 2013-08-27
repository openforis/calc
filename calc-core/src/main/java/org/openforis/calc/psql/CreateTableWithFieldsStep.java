package org.openforis.calc.psql;

import org.jooq.Field;
import org.jooq.Table;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class CreateTableWithFieldsStep extends ExecutablePsqlPart {

	CreateTableWithFieldsStep(Psql psql, Table<?> table, Field<?>[] fields) {
		super(psql);
		append("create table ");
		append(table);
		append(" (");
		for (int i = 0; i < fields.length; i++) {
			if ( i > 0 ) {
				append(", ");
			}
			append(fields[i].getName());
			append(" ");
			append(fields[i].getDataType().getTypeName());
		}
		append(")");
	}
}
