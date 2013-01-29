package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jooq.Field;
import org.jooq.Query;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author Mino Togna
 */
@Component 
@Transactional
public class SpecimenDao extends JooqDaoSupport<SpecimenRecord, Specimen> {

	private org.openforis.calc.persistence.jooq.tables.Specimen S = org.openforis.calc.persistence.jooq.tables.Specimen.SPECIMEN;
	private org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue SNV = org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue.SPECIMEN_NUMERIC_VALUE;
	private org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue SCV = org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE;
	
	private Factory batchCreate;
	private List<Query> batchQueries;
	
	public SpecimenDao() {
		super(SPECIMEN, Specimen.class, SPECIMEN.PLOT_SECTION_ID, SPECIMEN.SPECIMEN_NO);
		require(SPECIMEN.SPECIMEN_NO);
	}

	public void startBatch(){
		if( batchStarted() ) {
			throw new IllegalStateException("Batch already started");
		}
		batchCreate = getJooqFactory();
		batchQueries = new ArrayList<Query>();
	}
	
	public void executeBatch(){
		if( !batchStarted() ) {
			throw new IllegalStateException("Batch not started");
		}
		batchCreate.batch( batchQueries ).execute();
		
		closeBatch();
	}
	
	private void closeBatch() {
		batchCreate = null;
		batchQueries = null;
	}

	private boolean batchStarted(){
		return batchCreate != null;
	}

	@Transactional
	public Integer insert(int plotSectionId, int obsUnitId, FlatRecord r, Integer taxonId) {
		SpecimenRecord record = toJooqRecord(r);
		record.setPlotSectionId(plotSectionId);
		record.setObsUnitId(obsUnitId);
		record.setValue(pk(), null);
		record.setSpecimenTaxonId(taxonId);
		record.store();
		return record.getSpecimenId();
	}
	
	@Transactional
	public void batchInsert(int plotSectionId, int obsUnitId, FlatRecord r, Integer taxonId, Collection<VariableMetadata> variables) {
		if( !batchStarted() ) {
			throw new IllegalStateException("Batch not started");
		}
		
		Field<Long> s = Sequences.SPECIMEN_ID_SEQ.nextval();
		Long specimenId = batchCreate.select(s).fetchOne(s);
		Integer specimenNo = r.getValue("specimen_no", Integer.class);
		
		Query insert = batchCreate
				.insertInto(S, S.SPECIMEN_ID, S.PLOT_SECTION_ID, S.OBS_UNIT_ID, S.SPECIMEN_NO, S.SPECIMEN_TAXON_ID)
				.values(specimenId, plotSectionId, obsUnitId, specimenNo, taxonId);
		batchQueries.add( insert );
		
		batchInsertValues(specimenId, variables, r);
	}
	
	@Transactional
	private void batchInsertValues(long specimenId, Collection<VariableMetadata> variables, FlatRecord r ){
		for (VariableMetadata var : variables) {
			if ( var.isNumeric() ) {
				Double value = r.getValue(var.getVariableName(), Double.class);
				if ( value != null ) {
					Query insert = batchCreate
										.insertInto( SNV, SNV.SPECIMEN_ID, SNV.VARIABLE_ID, SNV.VALUE, SNV.COMPUTED)
										.values( specimenId, var.getVariableId(), value, false );
					
					batchQueries.add( insert );
				}
			} else {
				String code = r.getValue(var.getVariableName(), String.class);
				if ( code != null ) {
					Category cat = var.getCategoryByCode(code);
					Integer categoryId = cat.getCategoryId();
					Query insert = batchCreate
							.insertInto( SCV, SCV.SPECIMEN_ID, SCV.CATEGORY_ID, SCV.COMPUTED)
							.values( specimenId, categoryId, false );

					batchQueries.add( insert );
				}
				
			}
		}
	}
	
}
