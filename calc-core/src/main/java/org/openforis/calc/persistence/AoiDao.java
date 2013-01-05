package org.openforis.calc.persistence;

import org.openforis.calc.model.Aoi;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.records.AoiRecord;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Transactional
public class AoiDao extends JooqDaoSupport<AoiRecord, Aoi> {

	public AoiDao() {
		super(Tables.AOI, Aoi.class);
	}
}