package org.openforis.calc.service;
import java.util.ArrayList;
import java.util.List;

import org.openforis.calc.model.AoiHierarchy;
import org.openforis.calc.model.AoiHierarchyLevel;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.Survey;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.Variable;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.AoiHierarchyDao;
import org.openforis.calc.persistence.AoiHierarchyLevelDao;
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
	@Autowired
	private AoiHierarchyDao aoiHierarchyDao;
	@Autowired
	private AoiHierarchyLevelDao aoiHierarchyLevelDao;

	// TODO cache
	public SurveyMetadata getSurveyMetadata(String surveyName) {
		Survey survey = surveyDao.findByName(surveyName);
		if ( survey == null ) {
			return null;
		}
		List<ObservationUnitMetadata> oms = getObservationUnitMetadata(survey);
		List<AoiHierarchyMetadata> aoiHierarchies = getAoiHierarchyMetadata(survey);
		return new SurveyMetadata(survey, oms, aoiHierarchies);
	}

	private List<ObservationUnitMetadata> getObservationUnitMetadata(Survey survey) {
		List<ObservationUnit> units = observationUnitDao.findBySurveyId(survey.getId());
		List<ObservationUnitMetadata> oms = new ArrayList<ObservationUnitMetadata>();
		for ( ObservationUnit unit : units ) {
			ObservationUnitMetadata om = loadObservationMetadata(unit);
			oms.add(om);
		}
		return oms;
	}

	private List<AoiHierarchyMetadata> getAoiHierarchyMetadata(Survey survey) {
		List<AoiHierarchy> hierarchies = aoiHierarchyDao.findBySurveyId(survey.getSurveyId());
		List<AoiHierarchyMetadata> hms = new ArrayList<AoiHierarchyMetadata>();
		for (AoiHierarchy h : hierarchies) {
			List<AoiHierarchyLevel> levels = aoiHierarchyLevelDao.findByHierarchyId(h.getId());
			List<AoiHierarchyLevelMetadata> levelMetadata = AoiHierarchyLevelMetadata.fromList(levels);
			AoiHierarchyMetadata hm = new AoiHierarchyMetadata(h, levelMetadata);
			hms.add(hm);
		}
		return hms;
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
}
