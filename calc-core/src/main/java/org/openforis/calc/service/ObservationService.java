package org.openforis.calc.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.jooq.exception.DataAccessException;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SpecimenCategoricalValue;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.Taxon;
import org.openforis.calc.model.Variable;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.AreaFactDao;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenCategoricalValueDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenFactDao;
import org.openforis.calc.persistence.SpecimenNumericValueDao;
import org.openforis.calc.persistence.SpecimenViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author G. Miceli
 * @author Mino Togna
 *
 */
@Service
public class ObservationService extends CalcService {

	private static final String AREA_FACT_TABLE_NAME = "area_fact";
	private static final String SPECIMEN_FACT_TABLE_NAME_SUFFIX = "_fact";
	
	
	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenNumericValueDao specimenNumericValueDao;
	@Autowired
	private SpecimenCategoricalValueDao specimenCategoricalValueDao;
	@Autowired
	private SpecimenViewDao specimenViewDao;
	@Autowired
	private AreaFactDao areaFactDao;
	@Autowired
	private SpecimenFactDao specimenFactDao;
	@Autowired
	private TaxonService taxonService;
	
	public enum PlotDistributionCalculationMethod {
		SHARED_PLOT, PRIMARY_SECTION_ONLY;
	}
	
	@Transactional
	public void batchImportSpecimenData(String surveyName, String observationUnit, FlatDataStream in) throws IOException {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata specimenMetadata = getObservationUnitMetadata(surveyName, observationUnit);
		if ( specimenMetadata == null ) {
			throw new IllegalArgumentException("Invalid survey or observation unit");
		}
		String specimenUnitType = specimenMetadata.getObsUnitType();
		if ( !"specimen".equals(specimenUnitType) ) {
			throw new IllegalArgumentException("Invalid observation unit type: "+specimenUnitType); 
		}
		ObservationUnitMetadata plotMetadata = surveyMetadata.getObservationUnitMetadataByName("plot");;		// TODO <<< implement getParent
		int plotUnitId = plotMetadata.getObsUnitId();
		int specimenUnitId = specimenMetadata.getObsUnitId();
		
		Collection<VariableMetadata> variables = specimenMetadata.getVariableMetadata();
		
		specimenDao.startBatch();
		
		FlatRecord r;
		int rowCnt = 0;
		while ( (r = in.nextRecord()) != null ) {
			rowCnt++;
			
			Object[] key = plotSectionViewDao.extractKey(r, plotUnitId);
			Integer plotSectionId = plotSectionViewDao.getIdByKey(key);
			if ( plotSectionId == null ) {
				log().warn("Skipping specimen with unknown plot "+ Arrays.toString( key )  + " at row " + rowCnt);
				continue;
			} 
			if ( !specimenDao.isValid(r) ) {
				log().warn("Skipping invalid record: "+r + " at row " + rowCnt);
				continue;
			}
			try {
				String taxonCode = r.getValue(Taxon.TAXON_CODE_COLUMN_NAME, String.class);
				Taxon taxon = taxonService.findByTaxonCode(taxonCode);
				Integer taxonId = taxon != null ? taxon.getTaxonId() : null;
				if( taxonId == null ){
					Log log = log();
					if( log.isWarnEnabled() ){
						log.warn( "Invalid taxon code ("+taxonCode+") for tree " + Arrays.toString( key )  + " at row " + rowCnt);
					}
				}
				specimenDao.batchInsert(plotSectionId, specimenUnitId, r, taxonId, variables);
				
				if( rowCnt % 1000 == 0 ){
					specimenDao.executeBatch();
					specimenDao.startBatch();
				}
				
			} catch (DataAccessException e) {
				log().warn("Skipping record "+r+": "+e.getMessage() + " at row " + rowCnt);
			} 
			
		}
		specimenDao.executeBatch();
	}
	
	@Transactional
	@Deprecated()
	public void importSpecimenData(String surveyName, String observationUnit, FlatDataStream in) throws IOException {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata specimenMetadata = getObservationUnitMetadata(surveyName, observationUnit);
		if ( specimenMetadata == null ) {
			throw new IllegalArgumentException("Invalid survey or observation unit");
		}
		String specimenUnitType = specimenMetadata.getObsUnitType();
		if ( !"specimen".equals(specimenUnitType) ) {
			throw new IllegalArgumentException("Invalid observation unit type: "+specimenUnitType); 
		}
		ObservationUnitMetadata plotMetadata = surveyMetadata.getObservationUnitMetadataByName("plot");;		// TODO <<< implement getParent
		int plotUnitId = plotMetadata.getObsUnitId();
		int specimenUnitId = specimenMetadata.getObsUnitId();
		FlatRecord r;
		while ( (r = in.nextRecord()) != null ) {
			Object[] key = plotSectionViewDao.extractKey(r, plotUnitId);
			Integer plotSectionId = plotSectionViewDao.getIdByKey(key);
			if ( plotSectionId == null ) {
				log().warn("Skipping specimen with unknown plot "+key);
				continue;
			} 
			if ( !specimenDao.isValid(r) ) {
				log().warn("Skipping invalid record: "+r);
				continue;
			}
			try {
				String taxonCode = r.getValue(Taxon.TAXON_CODE_COLUMN_NAME, String.class);
				Taxon taxon = taxonService.findByTaxonCode(taxonCode);
				Integer taxonId = taxon != null ? taxon.getTaxonId() : null;
				if( taxonId == null ){
					Log log = log();
					if( log.isWarnEnabled() ){
						log.warn( "Invalid taxon code ("+taxonCode+") for tree " + Arrays.toString( key ) );
					}
				}
				Integer specimenId = specimenDao.insert(plotSectionId, specimenUnitId, r, taxonId);
				importValues(specimenMetadata, specimenId, r);
			} catch (DataAccessException e) {
				log().warn("Skipping record "+r+": "+e.getMessage());
			}
		}
	}

	public FlatDataStream getSpecimenDataStream(String surveyName, String observationUnitName, String[] fieldNames) {
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		ObservationUnitMetadata obsUnitParent = unitMetadata.getObsUnitParent();
		Collection<VariableMetadata> parentVariables = null;
		if( obsUnitParent != null ){
			parentVariables = obsUnitParent.getVariableMetadata();
		}
		return specimenViewDao.streamAll(variables, parentVariables, fieldNames, unitMetadata.getObsUnitId());
	}
	
	public FlatDataStream getPlotCategoryDistributionStream(String surveyName, String observationUnitName, PlotDistributionCalculationMethod plotDistributionCalculationMethod){
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		boolean useShares = plotDistributionCalculationMethod == PlotDistributionCalculationMethod.SHARED_PLOT;
		return plotSectionViewDao.streamCategoryDistribution(variables, unitMetadata.getObsUnitId(), useShares);
	}
	
	@Deprecated
	public FlatDataStream getAreaFactData(String surveyName, String observationUnitName, PlotDistributionCalculationMethod plotDistributionCalculationMethod){
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		boolean useShares = plotDistributionCalculationMethod == PlotDistributionCalculationMethod.SHARED_PLOT;
		return plotSectionViewDao.streamAreaFactData(variables, unitMetadata.getObsUnitId(), useShares);
	}
	
	@Deprecated
	public FlatDataStream getSpecimenFactData(String surveyName, String observationUnitName){
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		Collection<VariableMetadata> parentVariables = unitMetadata.getObsUnitParent().getVariableMetadata();
		return specimenDao.streamSpecimenFactData(unitMetadata.getObsUnitId(), variables , parentVariables );
	}
	
	private void importValues(ObservationUnitMetadata specimenMetadata, int specimenId, FlatRecord r) {
		Collection<VariableMetadata> vars = specimenMetadata.getVariableMetadata();
		for (VariableMetadata var : vars) {
//			Integer variableId = var.getVariableId();
//			String variableName = var.getVariableName();
			if ( var.isNumeric() ) {
				importSpecimenNumericValue(specimenId, r, var);
			} else {
				importSpecimenCategoricalValue(specimenId, r, var);
			}
		}
	}

	private void importSpecimenNumericValue(int specimenId, FlatRecord r, VariableMetadata var) {
		Double value = r.getValue(var.getVariableName(), Double.class);
		if ( value != null ) {
			SpecimenNumericValue snm = new SpecimenNumericValue();
			snm.setSpecimenId(specimenId);
			snm.setVariableId(var.getVariableId());
			snm.setValue(value);
			snm.setComputed( false );
			specimenNumericValueDao.insert(snm);
		}
	}

	private void importSpecimenCategoricalValue(int specimenId, FlatRecord r, VariableMetadata var) {
		String code = r.getValue(var.getVariableName(), String.class);
		if ( code != null ) {
			Category cat = var.getCategoryByCode(code);
			Integer categoryId = cat.getCategoryId();
			SpecimenCategoricalValue scm = new SpecimenCategoricalValue();
			scm.setCategoryId(categoryId);			
			scm.setSpecimenId(specimenId);
			scm.setComputed(false);
			specimenCategoricalValueDao.insert(scm);
		}
	}
	
	@Transactional
	@Deprecated
	public void updateAreaFacts(String surveyName, FlatDataStream data) {
		areaFactDao.createOrUpdateAreaFactTable(data, surveyName, AREA_FACT_TABLE_NAME);
	}

	@Transactional
	@Deprecated
	public void updateSpecimenFacts(String surveyName, String obsUnitName, FlatDataStream data) {
		String tableName = obsUnitName + SPECIMEN_FACT_TABLE_NAME_SUFFIX;
		specimenFactDao.createOrUpdateFactTable(data, surveyName, tableName);
	}
	
	@Transactional
	synchronized 
	public void updateSpecimenNumericalValue(String surveyName, String obsUnitName, FlatDataStream dataStream, String... variableNames) throws IOException {
		VariableMetadata[] variables = new VariableMetadata[variableNames.length];
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, obsUnitName);
		int i = 0;
		for ( String variableName : variableNames ) {
			VariableMetadata varMetadata = getVariableMetadata(unitMetadata, variableName);
			variables[i++] = varMetadata;
		}
		// Integer obsUnitId = varMetadata.getObsUnitId();
		specimenNumericValueDao.batchUpdate(unitMetadata.getObsUnitId(), dataStream, variables);
	}
	
	@Transactional
	synchronized
	public void updateSpecimenExpFactor(String surveyName, String obsUnitName, FlatDataStream dataStream) throws IOException {
//		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, obsUnitName);
		specimenDao.batchUpdateExpFactor( dataStream );
	}
	
//	private Reader convertDataToReader(List<String> data) {
//		StringBuilder builder = new StringBuilder();
//		for ( String string : data ) {
//			builder.append(string);
//			builder.append("\n");
//		}
//		String string = builder.toString();
//		return new StringReader(string);
//	}

	private VariableMetadata getVariableMetadata(ObservationUnitMetadata unitMetadata, String varName) {
		//ObservationUnitMetadata unitMetadata = getObsUnitMetadata(surveyName, obsUnitName);
		Integer obsUnitId = unitMetadata.getObsUnitId();
		
		VariableMetadata varMetadata = unitMetadata.getVariableMetadataByName( varName );
		if( varMetadata == null ){
			Variable variable = new Variable();
			variable.setObsUnitId(obsUnitId);
			variable.setVariableName(varName);
			variable.setType(Variable.Type.RATIO);
			variable.setForAnalysis( true );
			varMetadata = metadataService.insertVariable( variable, obsUnitId );
		}
		return varMetadata;
	}

}
