package org.openforis.calc.persistence.jooq.rolap;

import mondrian.olap.MondrianDef;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
public class InterviewCubeGenerator extends RolapCubeGenerator {

	private MondrianDefFactory mdf;

	InterviewCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		super(schemaGenerator, unit);
		mdf = schemaGenerator.getMondrianDefFactory();
	}

	@Override
	protected void initFactTable() {
		// Database
		InterviewFactTable dbTable = new InterviewFactTable(getDatabaseSchema(), getObservationUnitMetadata());
		setDatabaseFactTable(dbTable);
//		initAggregateTables(dbTable);
		
		// Mondrian
		MondrianDef.Table table = mdf.createTable(dbTable.getName());
		setMondrianTable(table);
	}

	@Override
	protected void initDimensionUsages() {
		initUserDefinedDimensionUsages();
	}

	@Override
	protected void initMeasures() {
		InterviewFactTable fact = (InterviewFactTable) getDatabaseFactTable();
		addMeasure(mdf.createMeasure(fact.COUNT, "Count"));
		initUserDefinedMeasures();
	}
}
