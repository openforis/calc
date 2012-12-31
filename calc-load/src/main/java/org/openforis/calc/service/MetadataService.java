package org.openforis.calc.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.openforis.calc.model.CategoricalVariable;
import org.openforis.calc.model.NumericVariable;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.SurveySourceMap;
import org.openforis.calc.persistence.CategoricalVariableDao;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.NumericVariableDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 *
 */
public class MetadataService {

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
	@Autowired
	private CollectSurveyIdmlBinder idmlBinder;
	
	public CollectSurvey loadIdml(String filename) throws FileNotFoundException, IdmlParseException {
		FileReader reader = new FileReader(filename);
		CollectSurvey cs = (CollectSurvey) idmlBinder.unmarshal(reader);
		return cs;
	}

	public SurveySourceMap loadSourceMap(int surveyId) {
		SurveySourceMap map = new SurveySourceMap();
		List<ObservationUnit> levels = surveyUnitDao.fetchBySurveyId(surveyId);
		for (ObservationUnit level : levels) {
			map.setModelObject(level.getSourceId(), level);
			// Load numeric variables
			List<NumericVariable> numvars = numericVariableDao.fetchByObservationUnit(level);
			for (NumericVariable var : numvars) {
				map.setModelObject(var.getSourceId(), var);
			}
			// Load categorical variables
			List<CategoricalVariable> catvars = categoricalVariableDao.fetchByObservationUnit(level);
			for (CategoricalVariable var : catvars) {
				map.setModelObject(var.getSourceId(), var);
			}
		}
		return map;
	}

}
