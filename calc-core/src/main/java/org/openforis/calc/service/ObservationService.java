package org.openforis.calc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.calc.model.ObservationUnit.Type;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.InterviewCategoricalValueDao;
import org.openforis.calc.persistence.InterviewDao;
import org.openforis.calc.persistence.InterviewNumericValueDao;
import org.openforis.calc.persistence.PlotCategoricalValueDao;
import org.openforis.calc.persistence.PlotFactDao;
import org.openforis.calc.persistence.PlotNumericVariableDao;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenCategoricalValueDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenNumericValueDao;
import org.openforis.calc.persistence.SpecimenViewDao;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Service
public class ObservationService extends CalcService {

	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private PlotSectionDao plotSectionDao;
	@Autowired
	private PlotNumericVariableDao plotNumericVariableDao;
	@Autowired
	private PlotCategoricalValueDao plotCategoricalValueDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenNumericValueDao specimenNumericValueDao;
	@Autowired
	private SpecimenCategoricalValueDao specimenCategoricalValueDao;
	@Autowired
	private SpecimenViewDao specimenViewDao;
	@Autowired
	private InterviewDao interviewDao;
	@Autowired
	private InterviewNumericValueDao interviewNumericValueDao;
	@Autowired
	private InterviewCategoricalValueDao interviewCategoricalValueDao;
	@Autowired
	private PlotFactDao specimenFactDao;
	@Autowired
	private TaxonService taxonService;
	
	public enum PlotDistributionCalculationMethod {
		SHARED_PLOT, PRIMARY_SECTION_ONLY;
	}

	public FlatDataStream getPlotSectionDataStream(String surveyName, String observationUnitName, String[] fieldNames) {
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		
		return plotSectionViewDao.streamAll(unitMetadata.getObsUnitId(), fieldNames, variables);
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
	
	@Deprecated
	public FlatDataStream getPlotCategoryDistributionStream(String surveyName, String observationUnitName, PlotDistributionCalculationMethod plotDistributionCalculationMethod){
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, observationUnitName);
		Collection<VariableMetadata> variables = unitMetadata.getVariableMetadata();
		boolean useShares = plotDistributionCalculationMethod == PlotDistributionCalculationMethod.SHARED_PLOT;
		return plotSectionViewDao.streamCategoryDistribution(variables, unitMetadata.getObsUnitId(), useShares);
	}

	@Transactional
	synchronized 
	public void updateSpecimenNumericValue(String surveyName, String obsUnitName, FlatDataStream dataStream, List<String> variableNames) throws IOException {
		List<VariableMetadata> variables = new ArrayList<VariableMetadata>(variableNames.size());
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, obsUnitName);
		for ( String variableName : variableNames ) {
			VariableMetadata varMetadata = unitMetadata.getVariableMetadataByName(variableName);
			if( varMetadata != null ) {
				variables.add( varMetadata );
			}
		}
		// Integer obsUnitId = varMetadata.getObsUnitId();
		specimenNumericValueDao.updateCurrentValue(unitMetadata.getObsUnitId(), dataStream, variables);
	}
	
	@Transactional
	synchronized
	public void updateSpecimenInlcusionArea(FlatDataStream dataStream) throws Exception {
		specimenDao.updateInclusionArea( dataStream );
	}
	
	@Transactional
	synchronized
	public void updatePlotSectionArea(FlatDataStream dataStream) throws IOException {
		plotSectionDao.updateArea(dataStream);
	}
	

	@Transactional
	synchronized 
	public void removeData(String surveyName, String obsUnitName) {
		ObservationUnitMetadata unit = getObservationUnitMetadata(surveyName, obsUnitName);
		if ( unit == null ) {
			return;
		}
		Type type = unit.getObsUnitTypeEnum();
		int unitId = unit.getId();
		switch (type) {
		case PLOT:
			plotNumericVariableDao.deleteByObsUnit(unitId);
			plotCategoricalValueDao.deleteByObsUnit(unitId);
			break;
		case SPECIMEN:
			specimenNumericValueDao.deleteByObsUnit(unitId);
			specimenCategoricalValueDao.deleteByObsUnit(unitId);
			break;
		case INTERVIEW:
			interviewNumericValueDao.deleteByObsUnit(unitId);
			interviewCategoricalValueDao.deleteByObsUnit(unitId);
			break;
		default:
			break;
		} 
	}
}
