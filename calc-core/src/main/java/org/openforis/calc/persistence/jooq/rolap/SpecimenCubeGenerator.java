package org.openforis.calc.persistence.jooq.rolap;

import mondrian.olap.MondrianDef;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
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
		// Database
		SpecimenFactTable dbTable = new SpecimenFactTable(getDatabaseSchema(), getObservationUnitMetadata());
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
		initUserDefinedMeasures();
	}
}
