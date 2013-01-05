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
	protected ObservationUnitDao observationUnitDao;
	@Autowired
	protected VariableDao variableDao;
	@Autowired
	protected CategoryDao categoryDao;
	@Autowired
	private CollectSurveyIdmlBinder idmlBinder;
	
	public CollectSurvey loadIdml(String filename) throws FileNotFoundException, IdmlParseException {
		FileReader reader = new FileReader(filename);
		CollectSurvey cs = (CollectSurvey) idmlBinder.unmarshal(reader);
		return cs;
	}

//	public ModelObjectSourceIdMap loadSourceIds(int surveyId) {
//		ModelObjectSourceIdMap map = new ModelObjectSourceIdMap();
//		List<ObservationUnit> levels = surveyUnitDao.findBySurveyId(surveyId);
//		for (ObservationUnit level : levels) {
//			map.putModelObject(level);
//			// Load variables
//			List<Variable> vars = variableDao.fetchByObservationUnit(level);
//			map.putModelObjects(vars);
//		}
//		return map;
//	}

	public void loadSurveyMetadata(Survey survey) {
		List<ObservationUnit> units = observationUnitDao.findBySurveyId(survey.getId());
		for (ObservationUnit unit : units) {
			loadObservationUnitMetadata(unit);
		}
		survey.setObservationUnits(units);
	}

	public void loadObservationUnitMetadata(ObservationUnit unit) {
		List<Variable> vars = variableDao.findByObservationUnitId(unit.getId());
		for (Variable var : vars) {
			if ( var.isCategorical() ) {
				List<Category> cats = categoryDao.findByVariableId(var.getId());
				var.setCategories(cats);
			}
		}
		unit.setVariables(vars);
	}
}
