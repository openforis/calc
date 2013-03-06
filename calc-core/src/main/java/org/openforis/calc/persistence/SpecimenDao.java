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
//	private org.openforis.calc.persistence.jooq.tables.SpecimenView SV = org.openforis.calc.persistence.jooq.tables.SpecimenView.SPECIMEN_VIEW;
//	private org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue SNV = org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue.SPECIMEN_NUMERIC_VALUE;
//	private org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue SCV = org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE;
//	private org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView SCVV = org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView.SPECIMEN_CATEGORICAL_VALUE_VIEW;
//	private org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView PCVV = org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView.PLOT_CATEGORICAL_VALUE_VIEW;
//	private org.openforis.calc.persistence.jooq.tables.PlotSection PS = org.openforis.calc.persistence.jooq.tables.PlotSection.PLOT_SECTION;
//	private org.openforis.calc.persistence.jooq.tables.PlotExpFactor PEF = org.openforis.calc.persistence.jooq.tables.PlotExpFactor.PLOT_EXP_FACTOR;
	
	public SpecimenDao() {
		super(SPECIMEN, Specimen.class, SPECIMEN.PLOT_SECTION_ID, SPECIMEN.SPECIMEN_NO);
		require(SPECIMEN.SPECIMEN_NO);
	}

//	@Transactional
//	public Integer insert(int plotSectionId, int obsUnitId, FlatRecord r, Integer taxonId) {
//		SpecimenRecord record = toJooqRecord(r);
//		record.setPlotSectionId(plotSectionId);
//		record.setObsUnitId(obsUnitId);
//		record.setValue(pk(), null);
//		record.setSpecimenTaxonId(taxonId);
//		record.store();
//		return record.getSpecimenId();
//	}
	
//	@Transactional
//	public void batchInsert(int plotSectionId, int obsUnitId, FlatRecord r, Integer taxonId, Collection<VariableMetadata> variables) {
//		if( !batchStarted() ) {
//			throw new IllegalStateException("Batch not started");
//		}
//		
//		Field<Long> s = Sequences.SPECIMEN_ID_SEQ.nextval();
//		Factory batchFactory = getBatchFactory();
//		
//		Long specimenId = batchFactory.select(s).fetchOne(s);
//		Integer specimenNo = r.getValue("specimen_no", Integer.class);
//		
//		Query insert = batchFactory
//				.insertInto(S, S.SPECIMEN_ID, S.PLOT_SECTION_ID, S.OBS_UNIT_ID, S.SPECIMEN_NO, S.SPECIMEN_TAXON_ID)
//				.values(specimenId, plotSectionId, obsUnitId, specimenNo, taxonId);
//		
//		addQueryToBatch( insert );
//		
//		batchInsertValues(specimenId, variables, r);
//	}
//	
//	@Transactional
//	private void batchInsertValues(long specimenId, Collection<VariableMetadata> variables, FlatRecord r ){
//		for (VariableMetadata var : variables) {
//			Factory batchFactory = getBatchFactory();
//			if ( var.isNumeric() ) {
//				Double value = r.getValue(var.getVariableName(), Double.class);
//				if ( value != null ) {
//					Query insert = batchFactory
//										.insertInto( SNV, SNV.SPECIMEN_ID, SNV.VARIABLE_ID, SNV.VALUE, SNV.COMPUTED)
//										.values( specimenId, var.getVariableId(), value, false );
//					
//					addQueryToBatch( insert );
//				}
//			} else {
//				String code = r.getValue(var.getVariableName(), String.class);
//				if ( code != null ) {
//					Category cat = var.getCategoryByCode(code);
//					Integer categoryId = cat.getCategoryId();
//					Query insert = batchFactory
//							.insertInto( SCV, SCV.SPECIMEN_ID, SCV.CATEGORY_ID, SCV.COMPUTED)
//							.values( specimenId, categoryId, false );
//
//					addQueryToBatch( insert );
//				}
//				
//			}
//		}
//	}
	
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
	
//	@Transactional
//	@Deprecated
//	synchronized
//	public FlatDataStream streamSpecimenFactData(int obsUnitId, Collection<VariableMetadata> variables, Collection<VariableMetadata> parentVariables) {
//		Set<String> dimensions = new HashSet<String>();
//		Set<String> measures = new HashSet<String>();
//		
//		Factory create = getJooqFactory();
//		SelectQuery select = create.selectQuery();
//		
//		//TODO commented out for compilation problems
////		select.addSelect( PEF.STRATUM_ID );
////		select.addSelect( PEF.AOI_ID );
//		select.addSelect( SV.SPECIMEN_TAXON_ID );
//		
//		select.addFrom(SV);
//		//TODO commented out for compilation problems		
////		select.addJoin( PEF, SV.STRATUM_ID.eq( PEF.STRATUM_ID ).and( PEF.AOI_ID.eq(1) ) );
//		select.addJoin( PS, SV.PLOT_SECTION_ID.eq( PS.PLOT_SECTION_ID) );
//				
//		select.addConditions( SV.SPECIMEN_OBS_UNIT_ID.eq(obsUnitId) );
//		//TODO commented out for compilation problems
////		select.addGroupBy( SV.PLOT_SECTION_ID, PEF.STRATUM_ID, PEF.AOI_ID, PEF.EXPF, PS.PLOT_SHARE, SV.SPECIMEN_TAXON_ID );
//		
//		int idx = 0;
//		for ( VariableMetadata var : parentVariables ) {
//			if( var.isCategorical() && var.isForAnalysis() ){
//				String variableName  = var.getVariableName();
//				
//				PlotCategoricalValueView C = PCVV.as( "c_" + (idx++) );
//				
//				select.addSelect( C.CATEGORY_ID.as( variableName ) );
//				select.addJoin( 
//							C, 
//							JoinType.LEFT_OUTER_JOIN, 
//							SV.PLOT_SECTION_ID.eq(C.PLOT_SECTION_ID)
//							.and( C.VARIABLE_ID.eq(var.getVariableId()) ) 
//							);
//				select.addGroupBy( C.CATEGORY_ID );
//				
//				dimensions.add(variableName);
//			}
//		}
//		
//		for ( VariableMetadata var : variables ) {
//			if ( var.isForAnalysis() ) {
//				String variableName = var.getVariableName();
//				Integer variableId = var.getVariableId();
//				
//				if ( var.isCategorical() ) {
//					
//					SpecimenCategoricalValueView C = SCVV.as( "c_" + (idx++) );
//					
//					select.addSelect(C.CATEGORY_ID .as(variableName));
//					select.addJoin(
//							C, 
//							JoinType.LEFT_OUTER_JOIN, 
//							SV.SPECIMEN_ID.eq(C.SPECIMEN_ID)
//								.and( C.VARIABLE_ID.eq(variableId) )
//							);
//					select.addGroupBy(C.CATEGORY_ID);
//					
//					dimensions.add(variableName);
//				} else if ( var.isNumeric() ) {
//					SpecimenNumericValue V = SNV.as( "v_" + (idx++) );
//					//TODO commented out for compilation problems
////					select.addSelect( V.VALUE.div(SV.SPECIMEN_EXP_FACTOR).sum().mul(PEF.EXPF).div(PS.PLOT_SHARE).mul(100).as(variableName) );
//					select.addJoin(V, JoinType.LEFT_OUTER_JOIN, SV.SPECIMEN_ID.eq(V.SPECIMEN_ID).and(V.VARIABLE_ID.eq(variableId)) );
//					
//					measures.add(variableName);
//				}
//			}
//		}
//		
//		
//		Table<Record> table = select.asTable().as( "fact_table" );
//		
//		SelectQuery selectFacts = create.selectQuery();
//		//TODO commented out for compilation problems
////		Field<?>[] dimFields = new Field<?>[]{ table.getField(PEF.AOI_ID.getName()) , table.getField(SV.SPECIMEN_TAXON_ID.getName()) };
////		selectFacts.addSelect( dimFields );
//		selectFacts.addFrom( table );
////		selectFacts.addGroupBy( dimFields );
//		
//		for ( String measure : measures ) {
//			selectFacts.addSelect( table.getField(measure).sum().as(measure) );
//		}
//		
//		for ( String dimension : dimensions ) {
//			Field<?> dimField = table.getField(dimension);
//			selectFacts.addSelect( Factory.coalesce(dimField, -1).as( dimField.getName() )  );
//			selectFacts.addGroupBy( dimField );
//		}
//		
//		if ( getLog().isDebugEnabled() ) {
//			getLog().debug("Creating specimen fact table");
//			getLog().debug(selectFacts.toString());
//		}
//
//		return stream( selectFacts.fetch() );
//	}
	
}
