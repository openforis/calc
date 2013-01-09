package org.openforis.calc.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.Variable;
import org.openforis.calc.persistence.CategoryDao;
import org.openforis.calc.persistence.ObservationUnitDao;
import org.openforis.calc.persistence.SurveyDao;
import org.openforis.calc.persistence.VariableDao;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class MetadataService {

	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private ObservationUnitDao observationUnitDao;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private CategoryDao categoryDao;
	@Autowired
	private CollectSurveyIdmlBinder idmlBinder;
	
	public CollectSurvey loadIdml(String filename) throws FileNotFoundException, IdmlParseException {
		FileReader reader = new FileReader(filename);
		CollectSurvey cs = (CollectSurvey) idmlBinder.unmarshal(reader);
		return cs;
	}

	public void loadSurveyMetadata(Survey survey) {
		List<ObservationUnit> units = observationUnitDao.findBySurveyId(survey.getId());
		for (ObservationUnit unit : units) {
			loadObservationUnitMetadata(unit);
		}
		survey.setObservationUnits(units);
	}

	private void loadObservationUnitMetadata(ObservationUnit unit) {
		List<Variable> vars = variableDao.findByObservationUnitId(unit.getId());
		for (Variable var : vars) {
			if ( var.isCategorical() ) {
				List<Category> cats = categoryDao.findByVariableId(var.getId());
				var.setCategories(cats);
			}
		}
		unit.setVariables(vars);
	}

	ObservationUnit getObservationUnit(String survey, String observationUnit) {
		Integer surveyId = surveyDao.getId(survey);
		if ( surveyId == null ) {
			return null;
		}
		return observationUnitDao.find(surveyId, observationUnit); 
	}
}
