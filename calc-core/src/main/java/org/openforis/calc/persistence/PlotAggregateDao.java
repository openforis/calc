package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.coalesce;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_STRATUM_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_EXPANSION_FACTOR;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import java.util.List;

import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.JoinType;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.AoiAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.openforis.calc.persistence.jooq.rolap.PlotAoiAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.PlotAoiStratumAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.PlotFactTable;
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
	public void populate(PlotAoiStratumAggregateTable aggTable) {
		//Minimum number of observations per substratum
		long threshold = 10;
		
		// Estimable areas
		SelectQuery select = createEstimableAggregateSelect(aggTable, threshold);
		Insert<Record> insert = createInsertFromSelect(aggTable, select);
		
		getLog().debug("Inserting estimable aggregate data:");
		getLog().debug(insert);
		
		insert.execute();
		
		// Non estimable areas
		select = createNonEstimableAggregateSelect(aggTable, threshold);
		insert = createInsertFromSelect(aggTable, select);
		
		getLog().debug("Inserting non estimable aggregate data:");
		getLog().debug(insert);
		
		insert.execute();
		
		getLog().debug("Complete");
	}
	
	
	@Transactional
	synchronized
	public void populate(PlotAoiAggregateTable aggTable) {
		Factory create = getJooqFactory();
		PlotAoiStratumAggregateTable plotAoiStratumAggTable = aggTable.getFactTable();
		PlotFactTable plotFactTable = plotAoiStratumAggTable.getFactTable();
		
		SelectQuery select = create.selectQuery();
		
		String estAreaFieldName = plotFactTable.EST_AREA.getName();
		
		select.addSelect(plotAoiStratumAggTable.AGG_COUNT.sum().as(plotAoiStratumAggTable.AGG_COUNT.getName()));
		select.addSelect(plotAoiStratumAggTable.COUNT.sum().as(plotAoiStratumAggTable.COUNT.getName()));
		select.addSelect(plotAoiStratumAggTable.getField(estAreaFieldName).sum().as(estAreaFieldName) );
		
		select.addFrom(plotAoiStratumAggTable);
		
		addAoisToAoiAggSelect(select, plotAoiStratumAggTable);
		addUserDefinedDimensionsToSelect(aggTable, select);
		
		
		@SuppressWarnings("unchecked")
		Insert insert = createInsertFromSelect(aggTable, select);
		getLog().debug("Inserting estimable aoi aggregate data:");
		getLog().debug(insert);
		
		insert.execute();
	}
	
	private void addAoisToAoiAggSelect(SelectQuery select, PlotAoiStratumAggregateTable plotAoiAggTable) {
		List<Field<Integer>> aoiFields = plotAoiAggTable.getAoiFields();
		select.addSelect(aoiFields);
		select.addGroupBy(aoiFields);
	}

	@SuppressWarnings("unchecked")
	private SelectQuery createEstimableAggregateSelect(PlotAoiStratumAggregateTable agg, long threshold) {
		PlotFactTable fact = agg.getFactTable();
//		ObservationUnitMetadata unit = fact.getObservationUnitMetadata();
//		SurveyMetadata survey = unit.getSurveyMetadata();
//		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
//		// TODO multiple AOI hierarchies
//		AoiHierarchyMetadata aoiHierarchy = aoiHierarchies.get(0);
		
		AoiHierarchyLevelMetadata aoiLevel = agg.getAoiHierarchyLevelMetadata();
		AoiHierarchyMetadata aoiHierarchy = aoiLevel.getAoiHierachyMetadata();
		String aoiLevelName = aoiLevel.getAoiHierarchyLevelName();
		
		Factory create = getJooqFactory();
		PlotExpansionFactor e = PLOT_EXPANSION_FACTOR.as("e");
		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		
		SelectQuery select = create.selectQuery();
		
		select.addSelect(s.STRATUM_ID);
		select.addSelect(coalesce(fact.COUNT.sum(), 1).as(agg.AGG_COUNT.getName()));
		select.addSelect(coalesce(fact.COUNT.sum(), 0).as(fact.COUNT.getName()));
		select.addSelect(coalesce(fact.COUNT.sum().mul(e.EXP_FACTOR), s.AREA).as(fact.EST_AREA.getName()) );
		
		select.addFrom(fact);
		
		select.addConditions( s.OBS_PLOT_CNT.gt(threshold) );
		
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
		
		addAoisToAggAoiStratumSelect(aoiHierarchy, aoiLevel.getAoiHierarchyLevelRank(), s, select, true);
		addUserDefinedDimensionsToSelect(agg, select);
		
		return select;
	}

	private SelectQuery createNonEstimableAggregateSelect(PlotAoiStratumAggregateTable agg, long threshold) {
		PlotFactTable plotFactTable = agg.getFactTable();
		
		AoiHierarchyLevelMetadata aoiLevel = agg.getAoiHierarchyLevelMetadata();
		AoiHierarchyMetadata aoiHierarchy = aoiLevel.getAoiHierachyMetadata();
		
		Factory create = getJooqFactory();

		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		SelectQuery select = create.selectQuery();
		
		select.addSelect(s.STRATUM_ID);
		select.addSelect(s.AREA.as(plotFactTable.EST_AREA.getName()));
		
		select.addFrom(s);
		
		select.addConditions(s.OBS_PLOT_CNT.lessOrEqual(threshold));
		
		addAoisToAggAoiStratumSelect(aoiHierarchy, aoiLevel.getAoiHierarchyLevelRank(), s, select, false);
		
		List<Field<Integer>> srcDimensions = plotFactTable.getUserDefinedDimensionFields();
		for ( Field<Integer> f : srcDimensions ) {
			String fieldName = f.getName();
			select.addSelect(Factory.val(-1).as(fieldName));
		}
		
		return select;
	}
	
	private void addAoisToAggAoiStratumSelect(AoiHierarchyMetadata aoiHierarchy, int aoiLevelRank, AoiStratumView s, SelectQuery select, boolean groupByAoi) {

		Aoi childAoi = null;
		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
		for (int i = aoiLevelRank-1; i >= 0; i--) {
			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
			Aoi a = AOI.as("a"+i);
			int aoiLevelId = level.getAoiHierarchyLevelId();
			String levelName = level.getAoiHierarchyLevelName();
			
			select.addSelect( a.AOI_ID.as(levelName) );
			
			if ( childAoi == null ) {
				select.addJoin(a, s.AOI_ID.eq(a.AOI_ID).and(a.AOI_HIERARCHY_LEVEL_ID.eq(aoiLevelId)));
			} else {
				select.addJoin(a,  childAoi.AOI_PARENT_ID.eq( a.AOI_ID ) );
			}
			
			if( groupByAoi ) {
				select.addGroupBy(a.AOI_ID);
			}
			
			childAoi = a;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void addUserDefinedDimensionsToSelect(AoiAggregateTable<?> agg, SelectQuery select) {
		FactTable fact = agg.getFactTable();
		List<Field<Integer>> srcDimensions = fact.getUserDefinedDimensionFields();
		for ( Field<Integer> f : srcDimensions ) {
			String fieldName = f.getName();
			select.addSelect(Factory.coalesce(f, -1).as(fieldName));
			select.addGroupBy(f);
		}
	}
}
