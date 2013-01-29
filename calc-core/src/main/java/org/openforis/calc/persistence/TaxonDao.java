package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.TAXON;

import org.openforis.calc.model.Taxon;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.TaxonRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@Component
@Transactional
public class TaxonDao extends JooqDaoSupport<TaxonRecord, Taxon> {

	public TaxonDao() {
		super(TAXON, Taxon.class, TAXON.TAXON_CODE);
		require(TAXON.TAXON_CODE);
	}

}
