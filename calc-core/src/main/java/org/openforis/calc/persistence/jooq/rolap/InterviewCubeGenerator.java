package org.openforis.calc.persistence.jooq.rolap;

import mondrian.olap.MondrianDef;

import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
public class InterviewCubeGenerator extends RolapCubeGenerator {

	InterviewCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		super(schemaGenerator, unit);
	}

	@Override
	protected void initFactTable() {
		// Database
		InterviewFactTable dbTable = new InterviewFactTable(getDatabaseSchema(), getObservationUnitMetadata());
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
