package org.openforis.calc.persistence.postgis;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public abstract class DdlStep {
	private Psql psql;
	private DdlStep previous;
	private StringBuilder sb;
	
	DdlStep(Psql psql) {
		this.psql = psql;
		this.sb = new StringBuilder();
	}

	DdlStep(DdlStep previous) {
		this.previous = previous;
		this.psql = previous.psql;
	}
	
	public int execute() {
		String sql = toString();
		return psql.execute(sql);
	}

	protected void append(Object s) {
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
}
