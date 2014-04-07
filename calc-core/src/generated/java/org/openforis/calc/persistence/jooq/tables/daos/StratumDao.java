/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables.daos;

import org.openforis.calc.metadata.Stratum;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class StratumDao extends org.jooq.impl.DAOImpl<org.openforis.calc.persistence.jooq.tables.records.StratumRecord, Stratum, java.lang.Integer> {

	/**
	 * Create a new StratumDao without any configuration
	 */
	public StratumDao() {
		super(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM, Stratum.class);
	}

	/**
	 * Create a new StratumDao with an attached configuration
	 */
	public StratumDao(org.jooq.Configuration configuration) {
		super(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM, Stratum.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(Stratum object) {
		return object.getId();
	}

	/**
	 * Fetch records that have <code>id IN (values)</code>
	 */
	public java.util.List<Stratum> fetchById(java.lang.Integer... values) {
		return fetch(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM.ID, values);
	}

	/**
	 * Fetch a unique record that has <code>id = value</code>
	 */
	public Stratum fetchOneById(java.lang.Integer value) {
		return fetchOne(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM.ID, value);
	}

	/**
	 * Fetch records that have <code>workspace_id IN (values)</code>
	 */
	public java.util.List<Stratum> fetchByWorkspaceId(java.lang.Integer... values) {
		return fetch(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM.WORKSPACE_ID, values);
	}

	/**
	 * Fetch records that have <code>stratum_no IN (values)</code>
	 */
	public java.util.List<Stratum> fetchByStratumNo(java.lang.Integer... values) {
		return fetch(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM.STRATUM_NO, values);
	}

	/**
	 * Fetch records that have <code>caption IN (values)</code>
	 */
	public java.util.List<Stratum> fetchByCaption(java.lang.String... values) {
		return fetch(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM.CAPTION, values);
	}

	/**
	 * Fetch records that have <code>description IN (values)</code>
	 */
	public java.util.List<Stratum> fetchByDescription(java.lang.String... values) {
		return fetch(org.openforis.calc.persistence.jooq.tables.StratumTable.STRATUM.DESCRIPTION, values);
	}
}