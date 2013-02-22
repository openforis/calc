package org.openforis.calc.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class CalcService {
	private Log log = LogFactory.getLog(getClass());

	@Autowired
	protected MetadataService metadataService;

	protected Log log() {
		return log;
	}

	protected ObservationUnitMetadata getObservationUnitMetadata(String surveyName, String observationUnitName) {
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata unitMetadata = surveyMetadata.getObservationUnitMetadataByName(observationUnitName);
		return unitMetadata;
	}
}
