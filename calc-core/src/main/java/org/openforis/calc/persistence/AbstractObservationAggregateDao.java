package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.coalesce;
import static org.jooq.impl.Factory.sum;
import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT_CNT_VIEW;

import java.util.List;

import org.jooq.Field;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.rolap.AggregateTable;
import org.openforis.calc.persistence.jooq.rolap.FactTable;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.AoiStratumView;
import org.openforis.calc.persistence.jooq.tables.SamplePlotCntView;
import org.springframework.transaction.annotation.Transactional;

@SuppressWarnings("rawtypes")
abstract class AbstractObservationAggregateDao<T extends AggregateTable<? extends FactTable>> extends JooqDaoSupport {

	private int threshold;

	@SuppressWarnings("unchecked")
	public AbstractObservationAggregateDao() {
		super(null, null);
		this.threshold = 30;
	}

	abstract public void populate(T aggTable);
	
	protected void addAoisToSelect(AoiHierarchyMetadata aoiHierarchy, int aoiLevelRank, AoiStratumView s, SelectQuery select, boolean groupByAoi) {

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
	protected void addUserDefinedDimensionsToSelect(FactTable fact, SelectQuery select) {
		List<Field<Integer>> srcDimensions = fact.getUserDefinedDimensionFields();
		for ( Field<Integer> f : srcDimensions ) {
			String fieldName = f.getName();
			
			select.addSelect( coalesce( f, -1 ).as( fieldName ) );
			select.addGroupBy( f );
		}
	}
	
	public int getThreshold(){
		return threshold;
	}
	
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	@Transactional
	protected SelectQuery createStratumSamplePlotCountQuery(int aoiHierarchyLevelId) {
		SamplePlotCntView c = SAMPLE_PLOT_CNT_VIEW.as("c");
		Aoi a = AOI.as("a");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		select.addSelect( c.STRATUM_ID );
		select.addSelect( sum(c.COUNT).as(c.COUNT.getName()) );
		
		select.addFrom( c );
		select.addJoin(
				a, 
				c.AOI_ID.eq(a.AOI_ID)
				.and( a.AOI_HIERARCHY_LEVEL_ID.eq(aoiHierarchyLevelId) )
				);
		select.addGroupBy( c.STRATUM_ID );
		
		return select;
	}
}
