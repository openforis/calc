package org.openforis.calc.service;

import java.io.IOException;
import java.util.Collection;

import org.jooq.exception.DataAccessException;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SpecimenCategoricalValue;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenCategoricalValueDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenNumericValueDao;
import org.openforis.calc.persistence.SpecimenViewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 
 * @author G. Miceli
 *
 */
@Service
public class ObservationService extends CalcService {

	@Autowired 
	private MetadataService metadataService;
	
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
	
	public enum PlotDistributionCalculationMethod {
		SHARED_PLOT, PRIMARY_SECTION_ONLY;
	}
	
//	@Transactional
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
				Integer specimenId = specimenDao.insert(plotSectionId, specimenUnitId, r);
				importValues(specimenMetadata, specimenId, r);
			} catch (DataAccessException e) {
				log().warn("Skipping record "+r+": "+e.getMessage());
			}
		}
	}

	public FlatDataStream getSpecimenDataStream(String surveyName, String observationUnitName, String[] fieldNames) {
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variableMetadata = unitMetadata.getVariableMetadata();
	
		return specimenViewDao.streamAll(variableMetadata, fieldNames, unitMetadata.getObsUnitId());
	}

	
	public FlatDataStream getPlotCategoryDistributionStream(String surveyName, String observationUnitName, PlotDistributionCalculationMethod plotDistributionCalculationMethod){
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variableMetadata = unitMetadata.getVariableMetadata();
		boolean useShares = plotDistributionCalculationMethod == PlotDistributionCalculationMethod.SHARED_PLOT;
		return plotSectionViewDao.getCategoryDistributionStream(variableMetadata, unitMetadata.getObsUnitId(), useShares);
	}
	
	private ObservationUnitMetadata getObservationUnitMetadata(String surveyName, String observationUnitName) {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata unitMetadata = surveyMetadata.getObservationUnitMetadataByName(observationUnitName);
		return unitMetadata;
	}
	
	private void importValues(ObservationUnitMetadata specimenMetadata, int specimenId, FlatRecord r) {
		Collection<VariableMetadata> vars = specimenMetadata.getVariableMetadata();
		for (VariableMetadata var : vars) {
//			Integer variableId = var.getVariableId();
//			String variableName = var.getVariableName();
			if ( var.isNumeric() ) {
				importNumericValue(specimenId, r, var);
			} else {
				importCategoricalValue(specimenId, r, var);
			}
		}
	}

	private void importNumericValue(int specimenId, FlatRecord r, VariableMetadata var) {
		Double value = r.getValue(var.getVariableName(), Double.class);
		if ( value != null ) {
			SpecimenNumericValue snm = new SpecimenNumericValue();
			snm.setSpecimenId(specimenId);
			snm.setVariableId(var.getVariableId());
			snm.setValue(value);
			snm.setComputed(false);
			specimenNumericValueDao.insert(snm);
		}
	}

	private void importCategoricalValue(int specimenId, FlatRecord r, VariableMetadata var) {
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
}
