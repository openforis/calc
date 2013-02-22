package org.openforis.calc.service;

import java.io.IOException;

import org.openforis.calc.importer.ImportException;
import org.openforis.calc.importer.SamplingDesignImporter;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Component
public class SamplingDesignService {

	@Autowired
	private MetadataService metadataService;
	@Autowired
	private SamplingDesignImporter importer;

	synchronized
	public void importSamplingDesign(String surveyName, String obsUnitName, String srsId, FlatDataStream stream) throws ImportException, IOException{
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata unitMetadata = surveyMetadata.getObservationUnitMetadataByName(obsUnitName);
		importer.setSurveyId(surveyMetadata.getSurveyId());
		importer.setUnitId(unitMetadata.getObsUnitId());		
		importer.setSrsId(srsId);
		importer.importData(stream);		
	}
}
