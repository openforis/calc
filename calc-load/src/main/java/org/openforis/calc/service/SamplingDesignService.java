package org.openforis.calc.service;

import org.openforis.calc.model.IdentifierMap;
import org.openforis.calc.model.PlotIdentifierMap;
import org.openforis.calc.model.SamplingDesignIdentifiers;
import org.openforis.calc.persistence.ClusterDao;
import org.openforis.calc.persistence.PlotDao;
import org.openforis.calc.persistence.StratumDao;
import org.openforis.calc.persistence.SurveyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 */
public class SamplingDesignService {

	@Autowired
	protected SurveyDao surveyDao;
	@Autowired
	protected StratumDao stratumDao;
	@Autowired
	protected ClusterDao clusterDao;
	@Autowired
	protected PlotDao plotDao;
	
	@Transactional
	public SamplingDesignIdentifiers loadGroundPlotIds(int surveyId) {
		SamplingDesignIdentifiers map = new SamplingDesignIdentifiers(surveyId);
		loadStratumIds(surveyId, map);
		loadClusterIds(surveyId, map);
		loadGroundPlotIds(surveyId, map);
		return map;
	}

	private void loadStratumIds(int surveyId, SamplingDesignIdentifiers map) {
		IdentifierMap ids = stratumDao.loadIdentifiers(surveyId);
		map.setStratumIds(ids);
	}

	private void loadClusterIds(int surveyId, SamplingDesignIdentifiers map) {
		IdentifierMap ids = clusterDao.loadIdentifiers(surveyId);
		map.setClusterIds(ids);
	}

	private void loadGroundPlotIds(int surveyId, SamplingDesignIdentifiers map) {
		PlotIdentifierMap ids = plotDao.loadGroundPlotIdentifiers(surveyId);
		map.setPlotIds(ids);
	}

}
