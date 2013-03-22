package org.openforis.calc.persistence;

import static org.jooq.impl.Factory.coalesce;
import static org.jooq.impl.Factory.sum;
import static org.openforis.calc.persistence.jooq.Tables.AOI_STRATUM_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_EXPANSION_FACTOR;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Record;
import org.jooq.Select;
import org.jooq.SelectQuery;
import org.jooq.impl.Factory;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.persistence.jooq.rolap.SpecimenAoiStratumAggregateTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenFactTable;
import org.openforis.calc.persistence.jooq.rolap.SpecimenPlotAggregateTable;
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
public class SpecimenAoiStratumAggregateDao extends AbstractObservationAggregateDao<SpecimenAoiStratumAggregateTable> {

	@Override
	@Transactional
	synchronized 
	public void populate(SpecimenAoiStratumAggregateTable aggTable) {
		Select<Record> select = createAoiStratumAggSelect(aggTable, getThreshold());
		
		@SuppressWarnings("unchecked")
		Query insert = createInsertFromSelect(aggTable, select);
		
		getLog().debug("Inserting specimen aoi stratum aggregate data:");
		getLog().debug(insert);

		insert.execute();

		getLog().debug("Complete");
	}
	
	
	
	@SuppressWarnings("unchecked")
	private SelectQuery createAoiStratumAggSelect(SpecimenAoiStratumAggregateTable aggTable, long threshold) {
		SpecimenPlotAggregateTable fact = aggTable.getFactTable();
		AoiHierarchyLevelMetadata aoiAggLevel = aggTable.getAoiHierarchyLevelMetadata();
		
		SpecimenFactTable specimenFact = fact.getFactTable();
		Field<?> taxonField = fact.getField( specimenFact.SPECIMEN_TAXON_ID.getName() );
	
		AoiStratumView s = AOI_STRATUM_VIEW.as("s");
		PlotExpansionFactor e = PLOT_EXPANSION_FACTOR.as("e");
		PlotSection p = PLOT_SECTION.as("p");
		
		Factory create = getJooqFactory();
		SelectQuery select = create.selectQuery();
	
		select.addSelect( s.STRATUM_ID );
		select.addSelect( taxonField );
		select.addSelect( coalesce(fact.COUNT.sum(), 1).as(aggTable.AGG_COUNT.getName()) );
		select.addSelect( coalesce(fact.COUNT.sum(), 0).as(fact.COUNT.getName()) );
		
		select.addFrom( s );
		
		select.addJoin(
				fact, 
				s.STRATUM_ID.eq( (Field<Integer>) fact.getField(s.STRATUM_ID.getName()) )
				.and( s.AOI_ID.eq( (Field<Integer>) fact.getField(aoiAggLevel.getAoiHierarchyLevelName()) ) )
				);
		select.addJoin(
				p,
				fact.PLOT_FIELD.eq( p.PLOT_SECTION_ID )
				);
		select.addJoin(
				e,
				s.STRATUM_ID.eq(e.STRATUM_ID)
				.and( s.AOI_ID.eq(e.AOI_ID) )
				);
		
		select.addConditions( s.OBS_PLOT_CNT.greaterOrEqual(threshold) );
		
		select.addGroupBy( s.STRATUM_ID );
		select.addGroupBy( e.EXP_FACTOR );
		select.addGroupBy( p.PLOT_SECTION_AREA );
		select.addGroupBy( taxonField );
		
		for ( Field<BigDecimal> measure : fact.getUserDefinedMeasureFields() ) {
			select.addSelect( sum( measure ).div( p.PLOT_SECTION_AREA ).mul( e.EXP_FACTOR ).as( measure.getName() ) );
		}
		
		addAoisToSelect(aoiAggLevel.getAoiHierachyMetadata(), aoiAggLevel.getAoiHierarchyLevelRank(), s, select, true);
		addUserDefinedDimensionsToSelect(aggTable.getFactTable(), select);
	
		return select;
	}
	
	
}
