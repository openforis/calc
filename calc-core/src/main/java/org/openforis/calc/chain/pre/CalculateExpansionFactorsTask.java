package org.openforis.calc.chain.pre;

import java.math.BigDecimal;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Select;
import org.jooq.SelectHavingStep;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.DynamicTable;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.metadata.SamplingDesign.ColumnJoin;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.ExpansionFactorTable;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.Phase1AoiTable;

/**
 * Task responsible for calculating the expansion factor for each stratum in all AOI levels. Results will be stored in a table called _expf in the output schema
 * 
 * @author M. Togna
 */
public final class CalculateExpansionFactorsTask extends Task {

	// public static final String EXPF_TABLE = "_expf";

	@Override
	protected void execute() throws Throwable {

		Workspace workspace = getWorkspace();
		InputSchema schema = getInputSchema();
		SamplingDesign samplingDesign = getSamplingDesign();

		// only one for now
		AoiHierarchy hierarchy = workspace.getAoiHierarchies().get(0);
		for (AoiLevel aoiLevel : hierarchy.getLevels()) {
			ExpansionFactorTable expf = schema.getExpansionFactorTable(aoiLevel);

			if (samplingDesign.getTwoPhases()) {
				// create expf table
				createExpfTable2phases(expf);
			} else {
				// TODO and TOTEST
			}

		}

	}


	private void createExpfTable2phases(ExpansionFactorTable expf) {
		DynamicTable<Record> phase1Table = new DynamicTable<Record>( getWorkspace().getPhase1PlotTable(), "calc" );
		Phase1AoiTable phase1AoiTable = getInputSchema().getPhase1AoiTable();
		AoiLevel aoiLevel = expf.getAoiLevel();
		boolean stratified = getSamplingDesign().getStratified();
		
		Field<Long> aoiIdField = phase1AoiTable.getAoiIdField(aoiLevel);
		Field<BigDecimal> aoiAreaField = phase1AoiTable.getAoiAreaField(aoiLevel);
		
		
		// select totals for aoi
		Select<?> selectTotals = psql()
			.select( phase1Table.getIdField().count() , aoiIdField )
			.from( phase1Table )
			.join( phase1AoiTable )
			.on( phase1Table.getIdField().eq(aoiIdField) )
			.groupBy( aoiIdField );
		Table<?> totals = selectTotals.asTable( "totals" );
		
		// select proportions
		SelectQuery<Record> select = psql().selectQuery();
		select.addSelect( phase1Table.getIdField().count() );
		select.addFrom( phase1Table );
		
		// join with phase1_aoi_table to calculate proportions. if no stratification is applied, then results will be the same
		select.addSelect( aoiIdField );
		select.addJoin( phase1AoiTable, phase1Table.getIdField().eq(phase1AoiTable.getIdField()) );
		select.addGroupBy( aoiIdField );
		
		if( stratified ) {
			ColumnJoin stratumJoin = getSamplingDesign().getStratumJoin();
			
			Field<Integer> stratumField = phase1Table.getIntegerField(stratumJoin.getColumn());
			select.addSelect( stratumField );
			select.addGroupBy( stratumField );
		}
		
		// join with totals inner query
		Field<?> totalField = totals.field("count");
		select.addSelect( totalField.as("total") );
		select.addJoin(totals, aoiIdField.eq( (Field<Long>)totals.field(aoiIdField.getName())) );
		select.addGroupBy( totalField );
		
		select.addSelect( phase1Table.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).as("proportion") );
		select.addSelect( phase1Table.getIdField().count().div( DSL.cast(totalField, Psql.DOUBLE_PRECISION) ).mul(aoiAreaField).as("area") );
		select.addGroupBy( aoiAreaField );
//		( count(p.id) / total.count::double precision ) as proportion,
//        ( count(p.id) / total.count::double precision ) * a._administrative_unit_level_1_area as area
		
		psql()
			.dropTableIfExists(expf)
			.execute();
		
		psql()
			.createTable(expf)
			.as( select )
			.execute();
			
		
	}
	
	@Override
	public String getName() {
		return "Calculate expansion factor";
	}
	
	protected SamplingDesign getSamplingDesign() {
		SamplingDesign samplingDesign = getWorkspace().getSamplingDesign();
		return samplingDesign;
	}
	
}
