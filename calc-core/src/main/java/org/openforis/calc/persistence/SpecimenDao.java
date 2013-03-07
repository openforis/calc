package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;

import java.io.IOException;

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

	@Deprecated	
	@Transactional
	synchronized
	public void batchUpdateExpFactor(FlatDataStream dataStream) throws IOException {
		startBatch();
		Factory create = getBatchFactory();
		
		FlatRecord r = null;
		while( (r=dataStream.nextRecord()) != null){
			int specimenId = r.getValue("specimen_id", Integer.class);
			double expFactor = r.getValue("exp_factor", Double.class);
			
			Query update = create
				.update( S )
				.set( S.SPECIMEN_EXP_FACTOR, expFactor )
				.where( S.SPECIMEN_ID.eq(specimenId) );
			addQueryToBatch( update );
		}
		
		executeBatch();
	}

	public Integer nextId() {
		Factory create = getJooqFactory();
		return create.nextval(Sequences.SPECIMEN_ID_SEQ).intValue();
	}
	

}
