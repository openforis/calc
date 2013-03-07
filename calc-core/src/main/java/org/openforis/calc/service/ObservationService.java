package org.openforis.calc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.PlotFactDao;
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
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenNumericValueDao specimenNumericValueDao;
	@Autowired
	private SpecimenCategoricalValueDao specimenCategoricalValueDao;
	@Autowired
	private SpecimenViewDao specimenViewDao;
	@Autowired
	private PlotFactDao specimenFactDao;
	@Autowired
	private TaxonService taxonService;
	
	public enum PlotDistributionCalculationMethod {
		SHARED_PLOT, PRIMARY_SECTION_ONLY;
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
		int i = 0;
		for ( String variableName : variableNames ) {
			VariableMetadata varMetadata = unitMetadata.getVariableMetadataByName(variableName);
			if( varMetadata != null ) {
				variables.add( varMetadata );
			}
		}
		// Integer obsUnitId = varMetadata.getObsUnitId();
		specimenNumericValueDao.updateCurrentValue(unitMetadata.getObsUnitId(), dataStream, variables);
	}
	
	@Deprecated
	@Transactional
	synchronized
	public void updateSpecimenExpFactor(String surveyName, String obsUnitName, FlatDataStream dataStream) throws IOException {
//		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata(surveyName, obsUnitName);
		specimenDao.batchUpdateExpFactor( dataStream );
	}
	

}
