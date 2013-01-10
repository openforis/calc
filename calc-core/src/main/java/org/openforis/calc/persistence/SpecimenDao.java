package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
@Component 
@Transactional
public class SpecimenDao extends JooqDaoSupport<SpecimenRecord, Specimen> {

	public SpecimenDao() {
		super(SPECIMEN, Specimen.class, SPECIMEN.PLOT_SECTION_ID, SPECIMEN.SPECIMEN_NO);
		require(SPECIMEN.SPECIMEN_NO);
	}

	public Integer insert(int plotSectionId, int obsUnitId, FlatRecord r) {
		SpecimenRecord record = toJooqRecord(r);
		record.setPlotSectionId(plotSectionId);
		record.setObsUnitId(obsUnitId);
		record.setValue(pk(), null);
		record.store();
		return record.getSpecimenId();
	}
}
