package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.AggName;

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

	PlotCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		super(schemaGenerator, unit);
	}

	@Override
	protected void initFactTable() {
		// Database
		PlotFactTable dbTable = new PlotFactTable(getDatabaseSchema(), getObservationUnitMetadata());
		setDatabaseFactTable(dbTable);
		initAggregateTables(dbTable);
		
		// Mondrian
		MondrianDef.Table table = createMondrianTable(dbTable.getName());
		setMondrianTable(table);
	}

	private void initAggregateTables(PlotFactTable factTable) {
		// Database
		PlotAoiStratumAggregateTable dbTable = new PlotAoiStratumAggregateTable(factTable, "district");
		addDatabaseTable(dbTable);
		
		// Mondrian
//		AggName aggName = new AggName();
		// TODO
//		addMondrianAggegateTable(aggName);
	}
	
	@Override
	protected void initDimensionUsages() {
		PlotFactTable fact = (PlotFactTable) getDatabaseFactTable();
		
		// Main key dimensions
		addDimensionUsage(createDimensionUsage("Stratum", fact.STRATUM_ID.getName()));
		addDimensionUsage(createDimensionUsage("Plot", fact.PLOT_ID.getName()));
		
		// AOI dimensions
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoi = survey.getAoiHierarchyMetadata();
		// TODO multiple hierarchies (one dimension per AOI hierarchy)
		AoiHierarchyMetadata hier = aoi.get(0);		
		String aoiDimName = RolapSchemaGenerator.toMdxName(hier.getAoiHierarchyName());
		String fk = hier.getMaxLevel().getAoiHierarchyLevelName();
		addDimensionUsage(createDimensionUsage(aoiDimName, fk));
		
		// User-defined dimensions 
		initUserDefinedDimensionUsages();
	}

	@Override
	protected void initMeasures() {
		PlotFactTable fact = (PlotFactTable) getDatabaseFactTable();
		addMeasure(createMeasure(fact.PLOT_LOCATION_DEVIATION.getName(), "Location deviation"));
		addMeasure(createMeasure(fact.EST_AREA.getName(), "Est. area"));
		addMeasure(createMeasure(fact.COUNT.getName(), "Count"));
		initUserDefinedMeasures();
	}
}
