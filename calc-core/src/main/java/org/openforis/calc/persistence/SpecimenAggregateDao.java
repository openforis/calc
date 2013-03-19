package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.coalesce;
import static org.jooq.impl.Factory.sum;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_STRATUM_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_EXPANSION_FACTOR;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.JoinType;
import org.jooq.Query;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.AoiAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenAoiStratumAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenFactTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenPlotAggregateTable;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.AoiStratumView;
import org.openforis.calc.persistence.jooq.tables.PlotExpansionFactor;
import org.openforis.calc.persistence.jooq.tables.PlotSection;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Component
@SuppressWarnings("rawtypes")
public class SpecimenAggregateDao extends JooqDaoSupport {

	@SuppressWarnings("unchecked")
	SpecimenAggregateDao() {
		super(null, null);
	}

	@Transactional
	synchronized 
	public void populate(SpecimenPlotAggregateTable aggTable) {

		SelectQuery select = createPlotAggSelect(aggTable);
		@SuppressWarnings("unchecked")
		Insert insert = createInsertFromSelect(aggTable, select);

		getLog().debug("Inserting specimen plot aggregate data:");
		getLog().debug(insert);

		insert.execute();

		getLog().debug("Complete");
	}

	@SuppressWarnings("unchecked")
	@Transactional
	synchronized 
	public void populate(SpecimenAoiStratumAggregateTable aggTable) {
		long threshold = 10;
		
		SelectQuery select = createAoiStratumAggSelect(aggTable, threshold);
		
		Query insert = createInsertFromSelect(aggTable, select);
		
		getLog().debug("Inserting specimen aoi stratum aggregate data:");
		getLog().debug(insert);

		insert.execute();

		getLog().debug("Complete");
	}

	@SuppressWarnings("unchecked")
	private SelectQuery createAoiStratumAggSelect(SpecimenAoiStratumAggregateTable aggTable, long threshold) {
		SpecimenPlotAggregateTable fact = aggTable.getFactTable();
		SpecimenFactTable specimenFact = fact.getFactTable();
		String taxonColName = specimenFact.SPECIMEN_TAXON_ID.getName();
		Field<?> taxonField = fact.getField( taxonColName );
		
		AoiHierarchyLevelMetadata aoiLevel = aggTable.getAoiHierarchyLevelMetadata();
		AoiHierarchyMetadata aoiHierarchy = aoiLevel.getAoiHierachyMetadata();
		String aoiLevelName = aoiLevel.getAoiHierarchyLevelName();
		
		//alias tables
		PlotExpansionFactor e = PLOT_EXPANSION_FACTOR.as("e");
		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		PlotSection p = PLOT_SECTION.as("p");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		select.addSelect( s.STRATUM_ID );
		select.addSelect( taxonField );
		select.addSelect( coalesce(fact.COUNT.sum(), 1).as(aggTable.AGG_COUNT.getName()) );
		select.addSelect( coalesce(fact.COUNT.sum(), 0).as(fact.COUNT.getName()) );
		
		select.addFrom(fact);
		
		select.addConditions( s.OBS_PLOT_CNT.gt(threshold ) );
		
		select.addJoin(
				p, 
				( (Field<Integer>) fact.getField(specimenFact.PLOT_ID.getName()) ).eq( p.PLOT_SECTION_ID )
				);
		
		select.addJoin(
				e, 
				e.AOI_ID.eq( (Field<Integer>) fact.getField(aoiLevelName) )
					.and( e.STRATUM_ID.eq( (Field<Integer>) fact.getField(STRATUM.STRATUM_ID.getName()) ) )
			);
		select.addJoin(
				s, 
				JoinType.RIGHT_OUTER_JOIN, 
				e.STRATUM_ID.eq( s.STRATUM_ID )
				.and(e.AOI_ID.eq(s.AOI_ID))				
			);
		
		select.addGroupBy( s.STRATUM_ID );
		select.addGroupBy( taxonField );
		select.addGroupBy( e.EXP_FACTOR );
		select.addGroupBy( p.PLOT_SHARE );
		select.addGroupBy( p.PLOT_SECTION_AREA );
//		Field<Double> expFactor = e.EXP_FACTOR.div( p.PLOT_SHARE ).mul( 100 );
		for ( Field<BigDecimal> measure : fact.getUserDefinedMeasureFields() ) {
			select.addSelect( sum(measure).div(p.PLOT_SECTION_AREA).mul( e.EXP_FACTOR ).as( measure.getName() ) );
		}
		
		addAoisToAggAoiStratumSelect(aoiHierarchy, aoiLevel.getAoiHierarchyLevelRank(), s, select, true);
		addUserDefinedDimensionsToSelect(aggTable, select);
		return select;
	}
	

	private SelectQuery createPlotAggSelect(SpecimenPlotAggregateTable aggTable) {
		SpecimenFactTable fact = aggTable.getFactTable();
		List<Field<Integer>> userDefinedDimensions = fact.getUserDefinedDimensionFields();
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();

		select.addSelect( fact.STRATUM_ID );
		select.addSelect( fact.CLUSTER_ID );
		select.addSelect( fact.PLOT_ID );
		select.addSelect( fact.SPECIMEN_TAXON_ID );
		select.addSelect( fact.getAoiFields() );
		select.addSelect( userDefinedDimensions );
		select.addSelect( sum(fact.COUNT).as(aggTable.AGG_COUNT.getName()) );
		select.addSelect( sum(fact.COUNT).as(aggTable.COUNT.getName()) );
		
		for ( Field<BigDecimal> measure : fact.getUserDefinedMeasureFields() ) {
			select.addSelect( sum( measure.div(fact.INCLUSION_AREA) ).mul(fact.PLOT_SECTION_AREA).as( measure.getName() ) );
		}

		select.addFrom(fact);
		
		select.addGroupBy(fact.STRATUM_ID);
		select.addGroupBy(fact.CLUSTER_ID);
		select.addGroupBy(fact.PLOT_ID);
		select.addGroupBy(fact.SPECIMEN_TAXON_ID);
		select.addGroupBy(fact.getAoiFields());
		select.addGroupBy(userDefinedDimensions);
		select.addGroupBy(fact.PLOT_SECTION_AREA);
		
		return select;
	}
	
	private void addAoisToAggAoiStratumSelect(AoiHierarchyMetadata aoiHierarchy, int aoiLevelRank, AoiStratumView s, SelectQuery select, boolean groupByAoi) {

		Aoi childAoi = null;
		List<AoiHierarchyLevelMetadata> aoiLevels = aoiHierarchy.getLevelMetadata();
		for (int i = aoiLevelRank-1; i >= 0; i--) {
			AoiHierarchyLevelMetadata level = aoiLevels.get(i);
			Aoi a = AOI.as( "a"+i );
			int aoiLevelId = level.getAoiHierarchyLevelId();
			String levelName = level.getAoiHierarchyLevelName();
			
			select.addSelect( a.AOI_ID.as(levelName) );
			
			if ( childAoi == null ) {
				select.addJoin( a, s.AOI_ID.eq(a.AOI_ID).and(a.AOI_HIERARCHY_LEVEL_ID.eq(aoiLevelId)) );
			} else {
				select.addJoin( a,  childAoi.AOI_PARENT_ID.eq( a.AOI_ID ) );
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
			
			select.addSelect( coalesce( f, -1 ).as( fieldName ) );
			select.addGroupBy( f );
		}
	}
}
