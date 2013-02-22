package org.openforis.calc.service;

import java.io.IOException;
import java.util.List;

import org.openforis.calc.importer.ImportException;
import org.openforis.calc.importer.SamplingDesignImporter;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.Cluster;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.model.Stratum;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.persistence.ClusterDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.SamplePlotDao;
import org.openforis.calc.persistence.StratumDao;
import org.openforis.calc.persistence.SurveyDao;
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
	private SurveyDao surveyDao;
	@Autowired
	private StratumDao stratumDao;
	@Autowired
	private ClusterDao clusterDao;
	@Autowired
	private ObservationUnitDao observationUnitDao;
	@Autowired
	private SamplePlotDao samplePlotDao;
	@Autowired
	private MetadataService metadataService;
	@Autowired
	private SamplingDesignImporter importer;
//	/**
//	 * Loads ground plots only
//	 * @param survey
//	 */
//	public void loadSamplingDesign(Survey survey) {
//		int surveyId = survey.getId();
//		List<Cluster> clusters = clusterDao.findBySurveyId(surveyId);
//		List<Stratum> strata = stratumDao.findBySurveyId(surveyId);
//		List<SamplePlot> groundPlots = samplePlotDao.findGroundPlotsBySurveyId(surveyId);
//		survey.setSamplingDesign(clusters, strata, groundPlots);
//	}
	
	public void importSamplingDesign(String surveyName, String obsUnitName, String srsId, FlatDataStream stream) throws ImportException, IOException{
		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata(surveyName);
		ObservationUnitMetadata unitMetadata = surveyMetadata.getObservationUnitMetadataByName(obsUnitName);
		
		importer.importSamplingDesign(surveyMetadata.getSurveyId(),unitMetadata.getObsUnitId(), srsId, stream);		
	}
	
	private List<Cluster> getClusters(Survey survey){
		int surveyId = survey.getId();
		List<Cluster> clusters = clusterDao.findBySurveyId(surveyId);
		return clusters;
	}
	
	public List<SamplePlot> getGroundPlots(Survey survey){
		int surveyId = survey.getId();
		List<Stratum> strata = stratumDao.findBySurveyId(surveyId);
		List<SamplePlot> groundPlots = samplePlotDao.findGroundPlotsBySurveyId(surveyId);
		List<Cluster> clusters = getClusters(survey);
		for ( SamplePlot plot : groundPlots ) {
			Integer clusterId = plot.getClusterId();
			for ( Cluster cluster : clusters ) {
				if(cluster.getClusterId().equals(clusterId)){
					plot.setCluster(cluster);
					break;
				}
			}
		}
		return groundPlots;
	}
	
}
