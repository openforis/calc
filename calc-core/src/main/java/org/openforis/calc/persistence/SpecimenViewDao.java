package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_VIEW;

import org.jooq.Field;
import org.openforis.calc.model.SpecimenView;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenViewRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component
@Transactional
public class SpecimenViewDao extends JooqDaoSupport<SpecimenViewRecord, SpecimenView> {

	private static final org.openforis.calc.persistence.jooq.tables.SpecimenView V = SPECIMEN_VIEW;
	
	public SpecimenViewDao() {
		super(SPECIMEN_VIEW, SpecimenView.class);
	}

	@Override
	protected Field<?> pk() {
		return V.SPECIMEN_ID;
	}

	

}
