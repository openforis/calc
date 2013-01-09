package org.openforis.calc.service;

import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.VariableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class ObservationService {

	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private ObservationUnitDao surveyUnitDao;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private CategoryDao categoryDao;
	
}
