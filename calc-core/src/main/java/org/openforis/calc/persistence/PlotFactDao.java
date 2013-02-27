package org.openforis.calc.persistence;
import static org.openforis.calc.persistence.jooq.Tables.*;

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
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel;
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
public class PlotFactDao extends RolapFactDao {

//	private static final String COUNT_COLUMN_NAME = "cnt";
//	private static final String EST_AREA_COLUMN_NAME = "est_area";

//	private static final String[] PLOT_POINTS = new String[] { P.PLOT_GPS_READING.getName(), P.PLOT_ACTUAL_LOCATION.getName(), P.PLOT_LOCATION.getName() };
//	private static final String[] PLOT_DIMENSIONS = new String[] { S.STRATUM_ID.getName(), C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName() };
//	private static final String[] AGG_STRATUM_AOI_EXCLUDED_DIMENSIONS = new String[] { C.CLUSTER_ID.getName(), P.PLOT_SECTION_ID.getName() };
//	private static final String[] PLOT_MEASURES = new String[] { P.PLOT_LOCATION_DEVIATION.getName(), COUNT_COLUMN_NAME, EST_AREA_COLUMN_NAME };
	
	@Override
	protected SelectQuery createFactSelect(ObservationUnitMetadata unit){
		Factory create = getJooqFactory();
		int unitId = unit.getObsUnitId();

		GroundPlotView p = GROUND_PLOT_VIEW.as("p");
		PlotSectionAoi pa = PLOT_SECTION_AOI.as("pa");
		SelectQuery select = create.selectQuery();
		select.addSelect(p.STRATUM_ID);
//		select.addSelect(p.CLUSTER_ID);
		select.addSelect(p.PLOT_SECTION_ID.as("plot_id"));
		select.addSelect(p.PLOT_LOCATION);
		select.addSelect(p.PLOT_GPS_READING);
		select.addSelect(p.PLOT_ACTUAL_LOCATION);
		select.addSelect(p.PLOT_LOCATION_DEVIATION);
		select.addSelect(Factory.val(1).as("cnt"));
		//TODO join with plot numeric value to get the right area
		select.addSelect(Factory.val(706.8583470577034).as("est_area"));
		
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
		int leafAoiRank = aoiHierarchy.getMaxRank();
		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
		
		Aoi a = AOI.as("a");
		AoiHierarchyLevel al = AOI_HIERARCHY_LEVEL.as("al");
		select.addJoin(a, a.AOI_ID.eq(pa.AOI_ID));
		select.addJoin(al, a.AOI_HIERARCHY_LEVEL_ID.eq(al.AOI_HIERARCHY_LEVEL_ID));
		select.addConditions(al.AOI_HIERARCHY_LEVEL_RANK.eq(leafAoiRank));
		
		Aoi childAoi = a;
		for (int i = 0; i < aoiLevels.size(); i++) {
			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
			String levelName = level.getAoiHierarchyLevelName();
			if ( i==0 ) {
				select.addSelect(pa.AOI_ID.as(levelName));
			} else {
				Aoi parentAoi = AOI.as("a"+i);
				
				select.addSelect(parentAoi.AOI_ID.as(levelName ));
				
				select.addJoin(parentAoi, childAoi.AOI_PARENT_ID.eq(parentAoi.AOI_ID));
				childAoi = parentAoi;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void addVariablesToSelect(ObservationUnitMetadata unit, GroundPlotView p, SelectQuery select) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();	
		int varIndex = 0;		
		for ( VariableMetadata variable : variables ) {
			if ( variable.isCategorical() && variable.isForAnalysis() ) {
				String varName = variable.getVariableName();
				varIndex += 1;
				PlotCategoricalValueView plotCatValueView = PLOT_CATEGORICAL_VALUE_VIEW.as("c_" + varIndex);
	
				select.addSelect( Factory.coalesce(plotCatValueView.CATEGORY_ID, -1).as(varName) );
	
				select.addJoin(
						plotCatValueView, 
						JoinType.LEFT_OUTER_JOIN, 
						p.PLOT_SECTION_ID.eq(plotCatValueView.PLOT_SECTION_ID)
							.and(plotCatValueView.VARIABLE_NAME.eq(varName)
						)
				);
			}
		}
	}
}
