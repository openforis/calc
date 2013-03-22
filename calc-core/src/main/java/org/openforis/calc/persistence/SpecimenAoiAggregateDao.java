/**
 * 
 */
package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.sum;
import static org.jooq.impl.Factory.value;
import static org.openforis.calc.persistence.jooq.Tables.AOI_STRATUM_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.SAMPLE_PLOT_CNT_VIEW;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.Insert;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.persistence.jooq.rolap.SpecimenAoiAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenAoiStratumAggregateTable;
import org.openforis.calc.persistence.jooq.tables.AoiStratumView;
import org.openforis.calc.persistence.jooq.tables.Specimen;
import org.openforis.calc.persistence.jooq.tables.Stratum;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@Component
public class SpecimenAoiAggregateDao extends AbstractObservationAggregateDao<SpecimenAoiAggregateTable> {

	@Transactional	
	@Override
	synchronized
	public void populate(SpecimenAoiAggregateTable aggTable) {
		Select<Record> estimableAreaAoiAggSelect = createNonSyntheticEstimatorAggSelect( aggTable, getThreshold() );
		Select<Record> nonEstimableAreaAoiAggSelect = createSyntheticEstimatorAggSelect( aggTable, getThreshold() );
		
		Select<Record> select = estimableAreaAoiAggSelect.union( nonEstimableAreaAoiAggSelect );
		
		@SuppressWarnings("unchecked")
		Insert<?> insert = createInsertFromSelect(aggTable, select);
		
		getLog().debug("Inserting specimen aoi aggregate data at " + aggTable.getAoiHierarchyLevelMetadata().getAoiHierarchyLevelName() + " level ");
		getLog().debug(insert);

		insert.execute();

		getLog().debug("Complete");
	}
	

	@SuppressWarnings("unchecked")
	@Transactional
	private SelectQuery createNonSyntheticEstimatorAggSelect(SpecimenAoiAggregateTable aggTable, long threshold) {
		SpecimenAoiStratumAggregateTable aoiStratumAggTable = aggTable.getFactTable();
		AoiHierarchyLevelMetadata aoiAggLevel = aoiStratumAggTable.getAoiHierarchyLevelMetadata();
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		
		Field<?> taxonField = aoiStratumAggTable.getField( Specimen.SPECIMEN.SPECIMEN_TAXON_ID.getName() );
		
		select.addSelect( taxonField );
		
		select.addFrom( s );

		select.addJoin( 
				aoiStratumAggTable, 
				s.STRATUM_ID.eq( (Field<Integer>) aoiStratumAggTable.getField(Stratum.STRATUM.STRATUM_ID.getName()))
				.and( s.AOI_ID.eq( (Field<Integer>) aoiStratumAggTable.getField(aoiAggLevel.getAoiHierarchyLevelName())) ) 
			);
				
		select.addConditions( s.OBS_PLOT_CNT.greaterOrEqual(threshold) );
		
		select.addGroupBy( taxonField );
		
		addAoisToSelect(aoiAggLevel.getAoiHierachyMetadata(), aoiAggLevel.getAoiHierarchyLevelRank(), s, select, true);
		addUserDefinedDimensionsToSelect(aggTable.getFactTable(), select);
		
		select.addSelect( sum(aoiStratumAggTable.AGG_COUNT).as(aoiStratumAggTable.AGG_COUNT.getName()) );
		select.addSelect( sum(aoiStratumAggTable.COUNT).as(aoiStratumAggTable.COUNT.getName()) );
		for ( Field<BigDecimal> measure : aoiStratumAggTable.getUserDefinedMeasureFields() ) {
			select.addSelect( sum(measure).as(measure.getName()) );
		}
		
		return select;
	}
	
	@SuppressWarnings("unchecked")
	@Transactional
	private SelectQuery createSyntheticEstimatorAggSelect(SpecimenAoiAggregateTable aggTable, long threshold) {
		SpecimenAoiStratumAggregateTable stratumAggTable = aggTable.getStratumAggTable();
		AoiHierarchyLevelMetadata aoiAggLevel = aggTable.getAoiHierarchyLevelMetadata();
		
		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
		
		Field<?> taxonField = stratumAggTable.getField( Specimen.SPECIMEN.SPECIMEN_TAXON_ID.getName() );
		select.addSelect( taxonField );
		select.addSelect( value(100).as( aggTable.AGG_COUNT.getName()) );
		select.addSelect( value(100).mul(10).as( aggTable.COUNT.getName()) );
		
		select.addFrom( s );
		
		select.addConditions( s.OBS_PLOT_CNT.lessThan(threshold) );
		
		select.addGroupBy( taxonField );
		select.addGroupBy( s.OBS_PLOT_CNT );
		
		select.addJoin(
				stratumAggTable,
				s.STRATUM_ID.eq( (Field<Integer>) stratumAggTable.getField(s.STRATUM_ID.getName()) )
				);
		
		addAoisToSelect(aoiAggLevel.getAoiHierachyMetadata(), aoiAggLevel.getAoiHierarchyLevelRank(), s, select, true);
		addUserDefinedDimensionsToSelect(stratumAggTable, select);
	
		Table<Record> stratumCnt = createStratumSamplePlotCountQuery( aoiAggLevel.getAoiHierarchyLevelId() ).asTable("c");
		select.addJoin(
				stratumCnt ,
				s.STRATUM_ID.eq( (Field<Integer>) stratumCnt.getField( s.STRATUM_ID.getName() ) )
				);
		
		
		Field<Long> stratumShare = s.SAMPLE_PLOT_CNT.div( (Field<? extends Number>) stratumCnt.getField(SAMPLE_PLOT_CNT_VIEW.COUNT.getName()) );		
		for ( Field<BigDecimal> measure : stratumAggTable.getUserDefinedMeasureFields() ) {
			// sum( o.volume * (s.sample_plot_cnt / c.count ) ) as volume
			select.addSelect( sum( measure.mul(stratumShare) ).as(measure.getName()) );
		}
		
		return select;
	}

}
