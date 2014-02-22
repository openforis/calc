package org.openforis.calc.psql;

import org.apache.commons.lang.StringUtils;
import org.jooq.Schema;
import org.jooq.Table;
import org.openforis.calc.psql.Psql.Privilege;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class GrantStep extends PsqlPart {

	GrantStep(Psql psql, Privilege... privileges) {
		super(psql);
		append("grant ");
		append(StringUtils.join(privileges, ", "));
	}

	public OnStep on(Table<?> table) {
		return new OnStep(table);
	}

	public OnStep onSchema(Schema schema) {
		return new OnStep(schema);
	}
	
	public class OnStep extends PsqlPart {
		OnStep(Table<?> table) {
			super(GrantStep.this);
			append("on ");
			append(table);
		}

		OnStep(Schema schema) {
			super(GrantStep.this);
			append("on schema ");
			append(schema);
		}

		public ToStep to(String user) {
			return new ToStep(user);
		}
		
		public class ToStep extends ExecutablePsqlPart {
			public ToStep(String user) {
				super(OnStep.this);
				append("to ");
				append("\"");
				append(user);
				append("\"");
			}
		}
	}
}
