package org.openforis.calc.persistence.jooq.rolap;

import mondrian.olap.MondrianDef;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
public class SpecimenCubeGenerator extends CubeGenerator {

	SpecimenCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		super(schemaGenerator, unit);
	}

	@Override
	protected void initFactTable() {
		// Database
		SpecimenFactTable dbTable = new SpecimenFactTable(getDatabaseSchema(), getObservationUnitMetadata());
		setDatabaseFactTable(dbTable);
//		initAggregateTables(dbTable);
		
		// Mondrian
		MondrianDef.Table table = createMondrianTable(dbTable.getName());
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
