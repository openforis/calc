package org.openforis.calc.service;

import org.openforis.calc.persistence.CategoricalVariableDao;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.NumericVariableDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataService {

	@Autowired
	protected SurveyDao surveyDao;
	@Autowired
	protected ObservationUnitDao surveyUnitDao;
	@Autowired
	protected NumericVariableDao numericVariableDao;
	@Autowired
	protected CategoricalVariableDao categoricalVariableDao;
	@Autowired
	protected CategoryDao categoryDao;
	
	
}
