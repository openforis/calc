package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.TAXONOMIC_CHECKLIST;

import org.openforis.calc.model.TaxonomicChecklist;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.TaxonomicChecklistRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@Component
@Transactional
public class TaxonomicChecklistDao extends JooqDaoSupport<TaxonomicChecklistRecord, TaxonomicChecklist> {

	public TaxonomicChecklistDao() {
		super(TAXONOMIC_CHECKLIST, TaxonomicChecklist.class, TAXONOMIC_CHECKLIST.CHECKLIST_NAME);
	}

}
