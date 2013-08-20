package org.openforis.calc.persistence.postgis;

import org.apache.commons.lang.StringUtils;
import org.jooq.Table;
import org.openforis.calc.persistence.postgis.Psql.Privilege;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class GrantStep extends DdlStep {

	GrantStep(Psql psql, Privilege... privileges) {
		super(psql);
		append("grant ");
		append(StringUtils.join(privileges, ", "));
	}

	public OnStep on(Table<?> table) {
		return new OnStep(table);
	}
	
	public class OnStep extends DdlStep {
		OnStep(Table<?> table) {
			super(GrantStep.this);
			append("on ");
			append(table);
		}

		public ToStep to(String user) {
			return new ToStep(user);
		}
		
		public class ToStep extends DdlStep {
			public ToStep(String user) {
				super(OnStep.this);
				append("to ");
				append(user);
			}
		}
	}
	
}
