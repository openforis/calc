package org.openforis.calc.persistence;
import static org.jooq.impl.Factory.coalesce;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_VIEW;

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
import org.openforis.calc.persistence.jooq.rolap.SpecimenFactTable;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.PlotCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.PlotSectionAoi;
import org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.tables.SpecimenView;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class SpecimenFactDao extends RolapFactDao<SpecimenFactTable> {

	@Override
	protected SelectQuery createFactSelect(SpecimenFactTable fact){
//		SpecimenFactTable specimenFact = (SpecimenFactTable) fact;
		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
		int unitId = unit.getObsUnitId();

		SpecimenView s = SPECIMEN_VIEW.as("s");
		PlotSectionAoi pa = PLOT_SECTION_AOI.as("pa");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		select.addSelect(s.STRATUM_ID);
		select.addSelect(s.CLUSTER_ID);
		select.addSelect(s.PLOT_SECTION_ID.as(fact.PLOT_ID.getName()));
		select.addSelect(s.SPECIMEN_ID);
		select.addSelect(s.SPECIMEN_TAXON_ID);
		select.addSelect(Factory.val(1).as(fact.COUNT.getName()));
		
		select.addFrom(s);
		
		select.addJoin(pa, s.PLOT_SECTION_ID.eq(pa.PLOT_SECTION_ID));
		
		select.addConditions(s.SPECIMEN_OBS_UNIT_ID.eq(unitId));
		
		addAoisToSelect(unit, pa, select);
		
		addUnitVariablesToSelect(unit, s, select);
		
		ObservationUnitMetadata parentUnit = unit.getObsUnitParent();
		if( parentUnit != null ) {
			addParentVariablesToSelect(parentUnit, s, select);
		}
		
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
		select.addJoin(a, 
					a.AOI_ID
						.eq( pa.AOI_ID )
						.and( a.AOI_HIERARCHY_LEVEL_ID.eq(leafLevel.getAoiHierarchyLevelId()) )
				);
//		select.addConditions(a.AOI_HIERARCHY_LEVEL_ID.eq(leafLevel.getAoiHierarchyLevelId()));
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
	private void addUnitVariablesToSelect(ObservationUnitMetadata unit, SpecimenView s, SelectQuery select) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();
		int idx = 0;
		for ( VariableMetadata var : variables ) {			
			if ( var.isForAnalysis() ) {
				
				Integer varId = var.getVariableId();
				String varName = var.getVariableName();
				idx += 1;
				
				if( var.isCategorical() ){
					SpecimenCategoricalValueView v = SPECIMEN_CATEGORICAL_VALUE_VIEW.as("v"+idx);
					
					select.addSelect( 
							coalesce( v.CATEGORY_ID , -1 ).as(varName)
							);
					
					select.addJoin(
							v,
							JoinType.LEFT_OUTER_JOIN,
							s.SPECIMEN_ID.eq(v.SPECIMEN_ID)
							.and(v.CURRENT.isTrue())
							.and(v.VARIABLE_ID.eq(varId))
							);
					
				} else if ( var.isNumeric() ){
					// join with specimen_numeric_value
					SpecimenNumericValue v = SPECIMEN_NUMERIC_VALUE.as("v"+idx);
					
					select.addSelect( v.VALUE.as(varName) );
					
					select.addJoin(
							v,
							JoinType.LEFT_OUTER_JOIN,
							s.SPECIMEN_ID.eq( v.SPECIMEN_ID )
							.and( v.VARIABLE_ID.eq(varId) )
							.and( v.CURRENT.isTrue() )
							);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addParentVariablesToSelect(ObservationUnitMetadata unit, SpecimenView view, SelectQuery select) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();	
		int idx = 0;		
		for ( VariableMetadata var : variables ) {
			if ( var.isCategorical() && var.isForAnalysis() ) {
				Integer varId = var.getVariableId();
				String varName = var.getVariableName();
				idx += 1;
				
				PlotCategoricalValueView c = PLOT_CATEGORICAL_VALUE_VIEW.as("c" + idx);
	
				select.addSelect( coalesce(c.CATEGORY_ID, -1).as(varName) );
	
				select.addJoin(
						c, 
						JoinType.LEFT_OUTER_JOIN, 
						view.PLOT_SECTION_ID.eq(c.PLOT_SECTION_ID)
						.and( c.VARIABLE_ID.eq(varId) )
						.and( c.CURRENT.isTrue() )
				
				);
			}
		}
	}
}
