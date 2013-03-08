package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.OBSERVATION_UNIT;
import static org.openforis.calc.persistence.jooq.Tables.TAXONOMIC_CHECKLIST;

import java.util.List;

import org.jooq.impl.Factory;
import org.openforis.calc.model.TaxonomicChecklist;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.TaxonomicChecklistRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class TaxonomicChecklistDao extends JooqDaoSupport<TaxonomicChecklistRecord, TaxonomicChecklist> {

	public TaxonomicChecklistDao() {
		super(TAXONOMIC_CHECKLIST, TaxonomicChecklist.class, TAXONOMIC_CHECKLIST.CHECKLIST_NAME);
	}

	public List<TaxonomicChecklist> findBySurveyId(int surveyId) {
		Factory create = getJooqFactory();
		
		List<TaxonomicChecklist> result = 
				create.select()
					.from(TAXONOMIC_CHECKLIST)
					.join(OBSERVATION_UNIT)
					.on(TAXONOMIC_CHECKLIST.OBS_UNIT_ID.eq(OBSERVATION_UNIT.OBS_UNIT_ID))
					.where(OBSERVATION_UNIT.SURVEY_ID.eq(surveyId))
					.fetch()
					.into(getType());
		
		return result;
	}

}
