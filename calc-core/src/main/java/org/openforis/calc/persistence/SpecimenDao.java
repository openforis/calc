package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;

import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component
@Transactional
public class SpecimenDao extends JooqDaoSupport<SpecimenRecord, Specimen> {

	private org.openforis.calc.persistence.jooq.tables.Specimen S = org.openforis.calc.persistence.jooq.tables.Specimen.SPECIMEN;

	public SpecimenDao() {
		super(SPECIMEN, Specimen.class, SPECIMEN.PLOT_SECTION_ID, SPECIMEN.SPECIMEN_NO);
		require(SPECIMEN.SPECIMEN_NO);
	}

	@Transactional
	synchronized 
	public void updateInclusionArea(FlatDataStream dataStream) throws Exception {
		try {
			startBatch();
			Factory create = getBatchFactory();

			FlatRecord r = null;
			while ( (r = dataStream.nextRecord()) != null ) {
				int specimenId = r.getValue("specimen_id", Integer.class);
				double inclArea = r.getValue("inclusion_area", Double.class);

				Query update = 
						create
							.update( S )
							.set( S.INCLUSION_AREA, inclArea )
							.where( S.SPECIMEN_ID.eq(specimenId) );
				
				addQueryToBatch(update);
			}

			executeBatch();
		} catch ( Exception e ) {
			closeBatch();
			throw e;
		}
	}

	public Integer nextId() {
		Factory create = getJooqFactory();
		return create.nextval(Sequences.SPECIMEN_ID_SEQ).intValue();
	}

	public void deleteByObsUnit(int id) {
		Factory create = getJooqFactory();
		org.openforis.calc.persistence.jooq.tables.Specimen s = SPECIMEN.as("s");
		create
			.delete(s)
			.where(s.OBS_UNIT_ID.eq(id))
			.execute();
	}
}
