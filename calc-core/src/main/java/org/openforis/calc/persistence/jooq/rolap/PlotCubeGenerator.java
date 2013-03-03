package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.AggTable;

import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PlotCubeGenerator extends RolapCubeGenerator {
	private List<AggTable> aggTables;
	private MondrianDefFactory mdf;

	PlotCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		super(schemaGenerator, unit);
		mdf = schemaGenerator.getMondrianDefFactory();
	}

	@Override
	protected void initFactTable() {
		aggTables = new ArrayList<AggTable>();
		
		// Database
		PlotFactTable dbTable = new PlotFactTable(getDatabaseSchema(), getObservationUnitMetadata());
		setDatabaseFactTable(dbTable);
		initAggregateTables(dbTable);

		// Mondrian
		MondrianDef.Table table = mdf.createTable(dbTable.getName());
//		if ( !mondrianAggregateTables.isEmpty() ) {
			table.aggTables = aggTables.toArray(new AggTable[0]);
//		}
		setMondrianTable(table);
	}

	private void initAggregateTables(PlotFactTable factTable) {
		// Database
		PlotAoiStratumAggregateTable dbTable = new PlotAoiStratumAggregateTable(factTable, "district");
		addDatabaseTable(dbTable);
		
		// Mondrian
//		AggName aggName = new AggName();
		// TODO
//		aggTables.add(aggName);
	}
	
	@Override
	protected void initDimensionUsages() {
		PlotFactTable fact = (PlotFactTable) getDatabaseFactTable();
		
		// Main key dimensions
		addDimensionUsage(mdf.createDimensionUsage("Stratum", fact.STRATUM_ID));
		addDimensionUsage(mdf.createDimensionUsage("Plot", fact.PLOT_ID));
		// TODO common place for fixed dimension names
		
		// AOI dimensions
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoi = survey.getAoiHierarchyMetadata();
		// TODO multiple hierarchies (one dimension per AOI hierarchy)
		AoiHierarchyMetadata hier = aoi.get(0);		
		String fk = hier.getMaxLevel().getAoiHierarchyLevelName();
		addDimensionUsage(mdf.createDimensionUsage(hier.getAoiHierarchyName(), hier.getAoiHierarchyName(),  fk));
		
		// User-defined dimensions 
		initUserDefinedDimensionUsages();
	}

	@Override
	protected void initMeasures() {
		PlotFactTable fact = (PlotFactTable) getDatabaseFactTable();
		addMeasure(mdf.createMeasure(fact.PLOT_LOCATION_DEVIATION, "Location deviation"));
		addMeasure(mdf.createMeasure(fact.EST_AREA, "Est. area"));
		addMeasure(mdf.createMeasure(fact.COUNT, "Count"));
		initUserDefinedMeasures();
	}
}
