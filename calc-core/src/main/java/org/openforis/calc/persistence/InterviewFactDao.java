package org.openforis.calc.persistence;
import static org.openforis.calc.persistence.jooq.Tables.INTERVIEW;
import static org.openforis.calc.persistence.jooq.Tables.INTERVIEW_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.INTERVIEW_NUMERIC_VALUE;

import java.util.Collection;

import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.openforis.calc.persistence.jooq.tables.Interview;
import org.openforis.calc.persistence.jooq.tables.InterviewCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.InterviewNumericValue;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G.Miceli
 */
@Component
@Transactional
public class InterviewFactDao extends RolapFactDao {

	@Override
	protected SelectQuery createFactSelect(FactTable fact){
//		InterviewFactTable interviewFact = (InterviewFactTable) fact;
		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
		Factory create = getJooqFactory();
		int unitId = unit.getObsUnitId();

//		GroundPlotView p = GROUND_PLOT_VIEW.as("p");
		Interview i = INTERVIEW.as("i");
		// TODO interview locations
//		InterviewAoi pa = INTERVIEW_AOI.as("pa");
		SelectQuery select = create.selectQuery();
		select.addSelect(i.CLUSTER_ID);
		select.addSelect(i.INTERVIEW_LOCATION);
		select.addSelect(Factory.val(1).as(fact.COUNT.getName()));
		//TODO join with plot numeric value to get the right area
		
		select.addFrom(i);
		
//		select.addJoin(pa, p.PLOT_SECTION_ID.eq(pa.PLOT_SECTION_ID));
		
		select.addConditions(i.OBS_UNIT_ID.eq(unitId));
		// Only primary sections, planned plots
		
		// TODO
//		addAoisToSelect(unit, pa, select);
		
		addVariablesToSelect(unit, i, select);
		
		return select;
	}

//	private void addAoisToSelect(ObservationUnitMetadata unit, PlotSectionAoi pa, SelectQuery select) {
//		SurveyMetadata survey = unit.getSurveyMetadata();
//		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
//		// TODO multiple AOI hierarchies
//		AoiHierarchyMetadata aoiHierarchy = aoiHierarchies.get(0);
//		AoiHierarchyLevelMetadata leafLevel = aoiHierarchy.getMaxLevel();
//		String leafLevelName = leafLevel.getAoiHierarchyLevelName();
//		
//		// Select leaf AOI
//		Aoi a = AOI.as("a");
//		select.addJoin(a, a.AOI_ID.eq(pa.AOI_ID));
//		select.addConditions(a.AOI_HIERARCHY_LEVEL_ID.eq(leafLevel.getAoiHierarchyLevelId()));
//		select.addSelect(pa.AOI_ID.as(leafLevelName));
//		
//		// Add select and join for non-leaf AOIs
//		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
//		Aoi table = a;
//		for (int i = aoiLevels.size()-2; i >= 0; i--) {
//			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
//			String levelName = level.getAoiHierarchyLevelName();
//			
//			Aoi parentTable = AOI.as("a"+i);
//			select.addSelect(parentTable.AOI_ID.as(levelName));
//			select.addJoin(parentTable, table.AOI_PARENT_ID.eq(parentTable.AOI_ID));
//			table = parentTable;
//		}
//	}

	@SuppressWarnings("unchecked")
	private void addVariablesToSelect(ObservationUnitMetadata unit, Interview interview, SelectQuery select) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();	
		int varIndex = 0;		
		for ( VariableMetadata var : variables ) {
			if ( !var.isForAnalysis() ) {
				continue;
			}
			Integer varId = var.getVariableId();
			String varName = var.getVariableName();
			varIndex += 1;
			if ( var.isCategorical() ) {
				InterviewCategoricalValueView catView = INTERVIEW_CATEGORICAL_VALUE_VIEW.as("var" + varIndex);
				select.addSelect( Factory.coalesce(catView.CATEGORY_ID, -1).as(varName) );
				select.addJoin(
						catView, 
						JoinType.LEFT_OUTER_JOIN, 
						interview.INTERVIEW_ID.eq(catView.INTERVIEW_ID)
							.and(catView.VARIABLE_ID.eq(varId)
						)
				);
			} else if ( var.isNumeric() ) {
				InterviewNumericValue nv = INTERVIEW_NUMERIC_VALUE.as("var" + varIndex);
				// TODO what to do about missing measures?!
				select.addSelect( Factory.coalesce(nv.VALUE, 0).as(varName));
				select.addJoin(
						nv, 
						JoinType.LEFT_OUTER_JOIN, 
						interview.INTERVIEW_ID.eq(nv.INTERVIEW_ID)
							.and(nv.VARIABLE_ID.eq(varId)
							.and(nv.CURRENT.isTrue())
						)
				);				
			}
		}
	}
}
