package org.openforis.calc.persistence;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.GROUND_PLOT_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;

import java.util.Collection;
import java.util.List;

import org.jooq.JoinType;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.rolap.PlotFactTable;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.GroundPlotView;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotSectionAoi;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class PlotFactDao extends RolapFactDao<PlotFactTable> {

	@Override
	protected SelectQuery createFactSelect(PlotFactTable fact){
		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
		Factory create = getJooqFactory();
		int unitId = unit.getObsUnitId();

		GroundPlotView p = GROUND_PLOT_VIEW.as("p");
		PlotSectionAoi pa = PLOT_SECTION_AOI.as("pa");
		SelectQuery select = create.selectQuery();
		select.addSelect(p.STRATUM_ID);
		select.addSelect(p.CLUSTER_ID);
		select.addSelect(p.SAMPLE_PLOT_ID.as("plot_id"));
		select.addSelect(p.PLOT_LOCATION);
		select.addSelect(p.PLOT_GPS_READING);
		select.addSelect(p.PLOT_ACTUAL_LOCATION);
		select.addSelect(p.PLOT_LOCATION_DEVIATION);
		select.addSelect(Factory.val(1).as(fact.COUNT.getName()));
		//TODO join with plot numeric value to get the right area
		select.addSelect(Factory.val(706.8583470577034).as(fact.EST_AREA.getName()));
		
		select.addFrom(p);
		
		select.addJoin(pa, p.PLOT_SECTION_ID.eq(pa.PLOT_SECTION_ID));
		
		select.addConditions(p.PLOT_OBS_UNIT_ID.eq(unitId));
		// Only primary sections, planned plots
		select.addConditions(p.VISIT_TYPE.eq("P"));
		select.addConditions(p.PRIMARY_SECTION.isTrue());
		
		addAoisToSelect(unit, pa, select);
		
		addVariablesToSelect(unit, p, select);
		
		return select;
	}

	private void addAoisToSelect(ObservationUnitMetadata unit, PlotSectionAoi pa, SelectQuery select) {
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
		// TODO multiple AOI hierarchies
		AoiHierarchyMetadata aoiHierarchy = aoiHierarchies.get(0);
		AoiHierarchyLevelMetadata leafLevel = aoiHierarchy.getMaxLevel();
		String leafLevelName = leafLevel.getAoiHierarchyLevelName();
		
		// Select leaf AOI
		Aoi a = AOI.as("a");
		select.addJoin(a, a.AOI_ID.eq(pa.AOI_ID));
		select.addConditions(a.AOI_HIERARCHY_LEVEL_ID.eq(leafLevel.getAoiHierarchyLevelId()));
		select.addSelect(pa.AOI_ID.as(leafLevelName));
		
		// Add select and join for non-leaf AOIs
		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
		Aoi table = a;
		for (int i = aoiLevels.size()-2; i >= 0; i--) {
			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
			String levelName = level.getAoiHierarchyLevelName();
			
			Aoi parentTable = AOI.as("a"+i);
			select.addSelect(parentTable.AOI_ID.as(levelName));
			select.addJoin(parentTable, table.AOI_PARENT_ID.eq(parentTable.AOI_ID));
			table = parentTable;
		}
	}

	@SuppressWarnings("unchecked")
	private void addVariablesToSelect(ObservationUnitMetadata unit, GroundPlotView view, SelectQuery select) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();	
		int varIndex = 0;		
		for ( VariableMetadata var : variables ) {
			if ( var.isCategorical() && var.isForAnalysis() ) {
				Integer varId = var.getVariableId();
				String varName = var.getVariableName();
				varIndex += 1;
				PlotCategoricalValueView catView = PLOT_CATEGORICAL_VALUE_VIEW.as("c" + varIndex);
	
				select.addSelect( Factory.coalesce(catView.CATEGORY_ID, -1).as(varName) );
	
				select.addJoin(
						catView, 
						JoinType.LEFT_OUTER_JOIN, 
						view.PLOT_SECTION_ID.eq(catView.PLOT_SECTION_ID)
						.and( catView.VARIABLE_ID.eq(varId) )
						.and( catView.CURRENT.isTrue() )
				);
			}
			// TODO select numeric variables (see InterviewFactDao)
			// TODO remove unneeded joins from num and cat value views (see interview_*_value_view)
		}
	}
}
