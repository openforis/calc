package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.*;
import static org.openforis.calc.persistence.PlotFactDao.*;
import static org.openforis.calc.persistence.jooq.Tables.*;

import java.util.List;

import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.AggregateTable;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.AoiStratumView;
import org.openforis.calc.persistence.jooq.tables.PlotExpansionFactor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 *
 */
@Component
@SuppressWarnings("rawtypes")
public class PlotAggregateDao extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	PlotAggregateDao() {
		super(null, null);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	synchronized
	public void populate(AggregateTable aggTable, int aoiLevelRank) {
		
		SelectQuery select = createAggregateSelect(aggTable, aoiLevelRank);
		Insert<Record> insert = createInsertFromSelect(aggTable, select);
		
		getLog().debug("Inserting aggregate data:");
		getLog().debug(insert);
		
		insert.execute();
		
		getLog().debug("Complete");
	}
	
	@SuppressWarnings("unchecked")
	private SelectQuery createAggregateSelect(AggregateTable agg, int aoiLevelRank) {
		FactTable fact = agg.getFactTable();
		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
		// TODO multiple AOI hierarchies
		AoiHierarchyMetadata aoiHierarchy = aoiHierarchies.get(0);
		AoiHierarchyLevelMetadata aoiLevel = aoiHierarchy.getLevelMetadata().get(aoiLevelRank-1);
		String aoiLevelName = aoiLevel.getAoiHierarchyLevelName();
		
		Factory create = getJooqFactory();
		PlotExpansionFactor e = PLOT_EXPANSION_FACTOR.as("e");
		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		
		SelectQuery select = create.selectQuery();
		
		select.addSelect(s.STRATUM_ID);
		select.addSelect(coalesce(fact.COUNT.sum(), 1).as(agg.AGG_COUNT.getName()));
		select.addSelect(coalesce(fact.COUNT.sum(), 0).as(fact.COUNT.getName()));
		select.addSelect(coalesce(fact.COUNT.sum().mul(e.EXP_FACTOR), s.AREA).as(EST_AREA_COLUMN_NAME) );
		
		
		select.addFrom(fact);
		
		select.addJoin(
				e, 
				e.AOI_ID.eq( (Field<Integer>) fact.getField(aoiLevelName))
					.and(e.STRATUM_ID.eq( (Field<Integer>) fact.getField(STRATUM.STRATUM_ID.getName())) )
			);
		select.addJoin(
				s, 
				JoinType.RIGHT_OUTER_JOIN, 
				e.STRATUM_ID.eq( s.STRATUM_ID )
				.and(e.AOI_ID.eq(s.AOI_ID))
			);
		
		select.addGroupBy(s.AREA);
		select.addGroupBy(e.EXP_FACTOR);
		select.addGroupBy(s.STRATUM_ID);
		addAoisToSelect(aoiHierarchy, aoiLevelRank, s, select);
		addDimensionsToSelect(agg, aoiHierarchy, select);
		
		return select;
	}

	private void addAoisToSelect(AoiHierarchyMetadata aoiHierarchy, int aoiLevelRank, AoiStratumView s, SelectQuery select) {

		Aoi childAoi = null;
		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
		for (int i = aoiLevelRank-1; i >= 0; i--) {
			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
			Aoi a = AOI.as("a"+i);
			int aoiLevelId = level.getAoiHierarchyLevelId();
			String levelName = level.getAoiHierarchyLevelName();
//			aoiNames.add(levelName);
			
			select.addSelect( a.AOI_ID.as(levelName) );
			
			if ( childAoi == null ) {
				select.addJoin(a, s.AOI_ID.eq(a.AOI_ID).and(a.AOI_HIERARCHY_LEVEL_ID.eq(aoiLevelId)));
			} else {
				select.addJoin(a,  childAoi.AOI_PARENT_ID.eq( a.AOI_ID ) );
			}
			
			select.addGroupBy(a.AOI_ID);
			
			childAoi = a;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addDimensionsToSelect(AggregateTable agg, AoiHierarchyMetadata aoiHierarchy, SelectQuery select) {
		FactTable fact = agg.getFactTable();
		List<TableField<Record, Integer>> srcDimensions = agg.getDimensionFields();
		for ( TableField<Record, Integer> f : srcDimensions ) {
			String fieldName = f.getName();
			// TODO take from field name
			if ( !aoiHierarchy.hasLevel(fieldName) && !fieldName.equals("stratum_id") ) {
				Field<?> srcFld = fact.getField(fieldName);
				select.addSelect(Factory.coalesce(srcFld, -1).as(fieldName));
				select.addGroupBy(srcFld);
			}
		}
	}
}
