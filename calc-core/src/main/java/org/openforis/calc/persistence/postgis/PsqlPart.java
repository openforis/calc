package org.openforis.calc.persistence.postgis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private Logger log;

	PsqlPart(Psql psql) {
		this.psql = psql;
		this.log = LoggerFactory.getLogger(getClass());
	}

	PsqlPart(PsqlPart previous) {
		this(previous.psql);
		this.previous = previous;
	}

	public int execute(Object... bindings) {
		String sql = toString();

		if ( bindings.length == 0 ) {
			log.debug(sql + ";");
		} else {
			log.debug(sql + "; -- Parameters: " + StringUtils.join(bindings) + "");
		}

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
}
