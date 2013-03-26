package org.openforis.calc.persistence;
import static org.jooq.impl.Factory.coalesce;
import static org.jooq.impl.Factory.val;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_AOI;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_CATEGORICAL_VALUE_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_NUMERIC_VALUE;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

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
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.openforis.calc.persistence.jooq.tables.PlotSectionAoi;
import org.openforis.calc.persistence.jooq.tables.SamplePlot;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValueView;
import org.openforis.calc.persistence.jooq.tables.SpecimenNumericValue;
import org.openforis.calc.persistence.jooq.tables.Stratum;
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
		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
		int unitId = unit.getObsUnitId();

		Specimen s = SPECIMEN.as("s");
		PlotSectionAoi pa = PLOT_SECTION_AOI.as("pa");
		Stratum st = STRATUM.as("st");
		SamplePlot sp = SAMPLE_PLOT.as("sp");
		PlotSection ps = PLOT_SECTION.as("ps");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		select.addSelect( st.STRATUM_ID );
		select.addSelect( sp.CLUSTER_ID );
		select.addSelect( s.PLOT_SECTION_ID.as(fact.PLOT_ID.getName()) );
		select.addSelect( s.SPECIMEN_ID );
		select.addSelect( s.SPECIMEN_TAXON_ID );
		select.addSelect( s.INCLUSION_AREA );
		select.addSelect( ps.PLOT_SECTION_AREA );		
		select.addSelect( val(1).as(fact.COUNT.getName()) );
		
		select.addFrom(s);
		
		select.addJoin( ps, s.PLOT_SECTION_ID.eq(ps.PLOT_SECTION_ID) );
		select.addJoin( sp , ps.SAMPLE_PLOT_ID.eq(sp.SAMPLE_PLOT_ID) );
		select.addJoin( st, sp.STRATUM_ID.eq(st.STRATUM_ID) );
		
		select.addJoin(pa, s.PLOT_SECTION_ID.eq(pa.PLOT_SECTION_ID));
		
		select.addConditions(s.OBS_UNIT_ID.eq(unitId));
		
		addAoisToSelect(unit, pa, select);
		
		addUnitVariablesToSelect(unit, s, select, fact);
		
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
	private void addUnitVariablesToSelect(ObservationUnitMetadata unit, Specimen s, SelectQuery select, SpecimenFactTable fact) {
		Collection<VariableMetadata> variables = unit.getVariableMetadata();
		int idx = 0;
		for ( VariableMetadata var : variables ) {			
			if ( var.isForAnalysis() ) {
				
				Integer varId = var.getVariableId();
				String varName = var.getVariableName();
				idx += 1;
				
				if( var.isCategorical() ) {
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
					//
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
	private void addParentVariablesToSelect(ObservationUnitMetadata unit, Specimen view, SelectQuery select) {
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
