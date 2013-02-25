package org.openforis.calc.service;
import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.Variable;
import org.openforis.calc.model.VariableMetadata;
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
public class MetadataService {

	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private ObservationUnitDao observationUnitDao;
	@Autowired
	private VariableDao variableDao;
	@Autowired
	private CategoryDao categoryDao;

	// TODO cache
	public SurveyMetadata getSurveyMetadata(String surveyName) {
		Survey survey = surveyDao.findByName(surveyName);
		if ( survey == null ) {
			return null;
		}
		List<ObservationUnit> units = observationUnitDao.findBySurveyId(survey.getId());
		List<ObservationUnitMetadata> oms = new ArrayList<ObservationUnitMetadata>();
		for ( ObservationUnit unit : units ) {
			ObservationUnitMetadata om = loadObservationMetadata(unit);
			oms.add(om);
		}
		return new SurveyMetadata(survey, oms);
	}
	
	private ObservationUnitMetadata loadObservationMetadata(ObservationUnit unit) {
		List<Variable> vars = variableDao.findByObservationUnitId(unit.getId());
		List<VariableMetadata> vms = new ArrayList<VariableMetadata>();
		for ( Variable var : vars ) {
			if ( var.isCategorical() ) {
				List<Category> cats = categoryDao.findByVariableId(var.getId());
				vms.add(new VariableMetadata(var, cats));
			} else {
				vms.add(new VariableMetadata(var));
			}
		}
		return new ObservationUnitMetadata(unit, vms);
	}
	
	public VariableMetadata insertVariable(Variable variable, int observationUnitIt){
		variableDao.insert(variable);
		Variable var = variableDao.findByName(variable.getVariableName(), observationUnitIt);
		return new VariableMetadata( var );
	}

	// public ObservationUnit getObservationUnit(String surveyName, String observationUnitName) {
	// Integer surveyId = surveyDao.getId(surveyName);
	// if ( surveyId == null ) {
	// return null;
	// }
	// return observationUnitDao.find(surveyId, observationUnitName);
	// }
}
