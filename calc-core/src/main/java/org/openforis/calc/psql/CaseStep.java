package org.openforis.calc.psql;

import org.jooq.Condition;

/**
 * 
 * @author G. Miceli
 *
 */
public class CaseStep extends PsqlPart {

	CaseStep(Psql psql) {
		super(psql);
		append("case");
	}

	CaseStep(CaseStep previous, Condition condition, Object expr) {
		super(previous);
		append(" when ");
		append(condition);
		append(" then ");
		append(expr);
	}

	public CaseStep when(Condition condition, Object expr) {
		return new CaseStep(this, condition, expr);
	}
	
	public ElseStep otherwise(Object expr) {
		return new ElseStep(expr);
	}
	
	public EndStep end() {
		return new EndStep(this);
	}
	
	class ElseStep extends PsqlPart {

		ElseStep(Object expr) {
			super(CaseStep.this);
			append("else ");
			append(expr);
		}
		
		public EndStep end() {
			return new EndStep(this);
		}
	}
	
	class EndStep extends PsqlPart {

		EndStep(PsqlPart previous) {
			super(previous);
			append(" end");
		}
	}
}
