package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.AggName;
import mondrian.olap.MondrianDef.AggTable;

import org.openforis.calc.model.AoiHierarchyLevelMetadata;
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
		
		setMondrianTable(table);
	}

	private void initAggregateTables(PlotFactTable factTable) {
		
		RolapSchemaGenerator schemaGenerator = getSchemaGenerator();
		SurveyMetadata surveyMetadata = schemaGenerator.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchyMetadata = surveyMetadata.getAoiHierarchyMetadata();
		for ( AoiHierarchyMetadata aoiHierarchy : aoiHierarchyMetadata ) {
			
			int rowCount = 50;
			PlotAoiStratumAggregateTable stratumAggTable = null;
			
			List<AoiHierarchyLevelMetadata> levels = aoiHierarchy.getLevelMetadata();
			for ( AoiHierarchyLevelMetadata level : levels ) {
				// Stratum/Aoi aggragates 
				// Database
				PlotAoiStratumAggregateTable aoiStratumAggTable = new PlotAoiStratumAggregateTable(factTable, level);
				addDatabaseTable(aoiStratumAggTable);

				if( level.getAoiHierarchyLevelRank() == 1 ) {
					stratumAggTable = aoiStratumAggTable;	
				}	
				
				
				// Aoi aggragates 
				// Database
				PlotAoiAggregateTable aoiAggTable = new PlotAoiAggregateTable(aoiStratumAggTable, level, stratumAggTable);
				addDatabaseTable(aoiAggTable);
				
				// Mondrian
				rowCount += 50;
				AggName aoiAggName = mdf.createAggregateName(aoiAggTable);
				aoiAggName.approxRowCount = rowCount + "";
				aggTables.add(aoiAggName);
				
				rowCount += 50;
				AggName aoiStratumAggName = mdf.createAggregateName(aoiStratumAggTable);
				aoiStratumAggName.approxRowCount = rowCount + "";
				aggTables.add(aoiStratumAggName);
			}
		}
		
	}
	
	@Override
	protected void initDimensionUsages() {
		PlotFactTable fact = (PlotFactTable) getDatabaseFactTable();
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		
		// Main key dimensions
		addDimensionUsage(mdf.createDimensionUsage("Stratum", fact.STRATUM_ID));
		addDimensionUsage(mdf.createDimensionUsage(unit.getDimensionTableName(), fact.PLOT_ID));
		// TODO common place for fixed dimension names
		
		// AOI dimensions
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
