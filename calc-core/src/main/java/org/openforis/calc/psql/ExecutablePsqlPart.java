package org.openforis.calc.psql;

/**
 * 
 * @author G. Miceli
 *
 */
public class ExecutablePsqlPart extends PsqlPart {

	public ExecutablePsqlPart(Psql psql) {
		super(psql);
	}

	public ExecutablePsqlPart(PsqlPart previous) {
		super(previous);
	}

	public int execute(Object... bindings) {
		String sql = toString();

		return getPsql().execute(sql, bindings);
	}
}
