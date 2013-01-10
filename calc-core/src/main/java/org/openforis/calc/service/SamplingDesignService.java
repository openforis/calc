package org.openforis.calc.service;

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
}
