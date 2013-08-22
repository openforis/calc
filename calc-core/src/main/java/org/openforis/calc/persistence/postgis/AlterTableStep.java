package org.openforis.calc.persistence.postgis;

import org.apache.commons.lang.StringUtils;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.engine.Worker;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class AlterTableStep extends DdlStep {

	AlterTableStep(Psql psql, Table<?> table) {
		super(psql);
		append("alter table ");
		append(table);
	}
	
	public AddPrimaryKeyStep addPrimaryKey(UniqueKey<?> key) {
		return new AddPrimaryKeyStep(key);
	}
	
	public AddColumnStep addColumn(Field<?> field) {
		return addColumn(field, true);
	}
	
	public AddColumnStep addColumn(Field<?> field, boolean nullable) {
		return new AddColumnStep(field, nullable);
	}

	public class AddPrimaryKeyStep extends DdlStep {
		AddPrimaryKeyStep(UniqueKey<?> key) {
			super(AlterTableStep.this);
			append("add primary key (");
			append(StringUtils.join(key.getFields().iterator(),", "));
			append(")");
		}
	}
	

	public class AddColumnStep extends DdlStep {
		AddColumnStep(Field<?> field, boolean nullable) {
			super(AlterTableStep.this);
			append("add column ");
			append(field.getName());
			append(" ");
			append(field.getDataType().getSQLDataType());
			if ( !nullable ) {
				append(" not null");
			}
		}
	}
}
