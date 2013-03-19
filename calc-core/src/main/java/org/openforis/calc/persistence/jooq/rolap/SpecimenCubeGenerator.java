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
import org.openforis.calc.model.TaxonomicChecklistMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class SpecimenCubeGenerator extends RolapCubeGenerator {

	private MondrianDefFactory mdf;

	SpecimenCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		super(schemaGenerator, unit);
		mdf = schemaGenerator.getMondrianDefFactory();
	}

	@Override
	protected void initFactTable() {
		aggTables = new ArrayList<AggTable>();

		// Database
		SpecimenFactTable dbTable = new SpecimenFactTable(getDatabaseSchema(), getObservationUnitMetadata());
		setDatabaseFactTable(dbTable);

		initAggregateTables(dbTable);

		// Mondrian
		MondrianDef.Table table = mdf.createTable(dbTable.getName());
		setMondrianTable(table);
	}

	@Override
	protected void initDimensionUsages() {
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		SurveyMetadata survey = unit.getSurveyMetadata();

		SpecimenFactTable fact = (SpecimenFactTable) getDatabaseFactTable();
		// Main key dimensions
		addDimensionUsage(mdf.createDimensionUsage("Stratum", fact.STRATUM_ID));
		addDimensionUsage(mdf.createDimensionUsage(unit.getDimensionTableName(), fact.SPECIMEN_ID));

		TaxonomicChecklistMetadata checklistMetadata = unit.getTaxonomicChecklistMetadata();
		if ( checklistMetadata != null ) {
			String taxonDimName = MondrianDefFactory.toMdxName(checklistMetadata.getTableName());
			addDimensionUsage(mdf.createDimensionUsage(taxonDimName, fact.SPECIMEN_TAXON_ID));
		}
		// TODO common place for fixed dimension names

		// AOI dimensions
		List<AoiHierarchyMetadata> aoi = survey.getAoiHierarchyMetadata();
		// TODO multiple hierarchies (one dimension per AOI hierarchy)
		AoiHierarchyMetadata hier = aoi.get(0);
		String fk = hier.getMaxLevel().getAoiHierarchyLevelName();
		addDimensionUsage(mdf.createDimensionUsage(hier.getAoiHierarchyName(), hier.getAoiHierarchyName(), fk));

		initUserDefinedDimensionUsages();
	}

	@Override
	protected void initMeasures() {
		SpecimenFactTable fact = (SpecimenFactTable) getDatabaseFactTable();
		addMeasure( mdf.createMeasure(fact.COUNT, "Count") );
		addMeasure( mdf.createMeasure(fact.INCLUSION_AREA, "", false) );
		addMeasure( mdf.createMeasure(fact.PLOT_SECTION_AREA, "", false) );
		initUserDefinedMeasures();
	}

	private void initAggregateTables(SpecimenFactTable factTable) {
		SpecimenPlotAggregateTable plotAggTable = new SpecimenPlotAggregateTable(factTable);
		addDatabaseTable(plotAggTable);

		AggName plotAggName = mdf.createAggregateName(plotAggTable);
		aggTables.add(plotAggName);
		// TODO stratum / aoi agg levels

		RolapSchemaGenerator schemaGenerator = getSchemaGenerator();
		SurveyMetadata surveyMetadata = schemaGenerator.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchyMetadata = surveyMetadata.getAoiHierarchyMetadata();
		for ( AoiHierarchyMetadata aoiHierarchy : aoiHierarchyMetadata ) {
			List<AoiHierarchyLevelMetadata> levels = aoiHierarchy.getLevelMetadata();
			for ( AoiHierarchyLevelMetadata level : levels ) {
				
				SpecimenAoiStratumAggregateTable aoiStratumAggTable = new SpecimenAoiStratumAggregateTable(plotAggTable, level);
				addDatabaseTable(aoiStratumAggTable);
				
				AggName aoiStratumAggName = mdf.createAggregateName(aoiStratumAggTable);
				aggTables.add(aoiStratumAggName);
				
				
			}
		}

	}

	@Override
	protected String getCubeName() {
		return "hidden_" + getObservationUnitMetadata().getObsUnitName();
	}
	
}
