package org.openforis.calc.psql;


/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public abstract class PsqlPart {
	private Psql psql;
	private PsqlPart previous;
	private StringBuilder sb;

	PsqlPart(Psql psql) {
		this.psql = psql;
	}

	PsqlPart(PsqlPart previous) {
		this(previous.psql);
		this.previous = previous;
	}

	public int execute(Object... bindings) {
		String sql = toString();

		psql.logSql(sql, bindings);

		return psql.execute(sql, bindings);
	}

	protected void append(Object s) {
		if ( sb == null ) {
			this.sb = new StringBuilder();
		}
		sb.append(s);
	}

	protected String toSql() {
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if ( previous != null ) {
			sb.append(previous.toString());
			sb.append(" ");
		}
		sb.append(this.toSql());
		return sb.toString();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
}
