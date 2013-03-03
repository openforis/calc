package org.openforis.calc.persistence.jooq.rolap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.DimensionUsage;
import mondrian.olap.MondrianDef.Measure;
import mondrian.olap.MondrianDef.Table;

import org.openforis.calc.model.ObservationUnit.Type;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;
/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public abstract class RolapCubeGenerator {
	
	// IN
	private String databaseSchema;
	private ObservationUnitMetadata unit;
	private RolapSchemaGenerator schemaGenerator;

	// INTERNAL
	private List<RolapTable> databaseTables;
	private FactTable databaseFactTable;
	private List<DimensionUsage> dimensionUsages;
	private List<Measure> measures;
	private Table mondrianTable;
	private MondrianDefFactory mdf;

	// OUT
//	private Cube cube;
	
	RolapCubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		this.schemaGenerator = schemaGenerator;
		this.databaseSchema = schemaGenerator.getDatabaseSchema();
		mdf = schemaGenerator.getMondrianDefFactory();
		this.unit = unit;
	}
	
	Cube createCube() {
		databaseTables = new ArrayList<RolapTable>();
		dimensionUsages = new ArrayList<DimensionUsage>();
		measures = new ArrayList<Measure>();
		
		initFactTable();
		initDimensionUsages();
		initMeasures();

		return mdf.createCube(unit.getObsUnitName(), mondrianTable, dimensionUsages, measures);
	}

	List<RolapTable> getDatabaseTables() {
		return databaseTables;
	}
	
	public String getDatabaseSchema() {
		return databaseSchema;
	}
	
	public static RolapCubeGenerator createInstance(RolapSchemaGenerator schemaGen, ObservationUnitMetadata unit) {
		Type unitType = unit.getObsUnitTypeEnum();
		switch (unitType) {
		case PLOT:
			return new PlotCubeGenerator(schemaGen, unit);
		case SPECIMEN:
			return new SpecimenCubeGenerator(schemaGen, unit);
		case INTERVIEW:
			return new InterviewCubeGenerator(schemaGen, unit);
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Create fact table and aggregates. Should add new databases tables
	 * with addDatabaseTable and Mondrian defs with setMondrianTable and
	 * addMondrianAggregateTable
	 * @return
	 */
	protected abstract void initFactTable();

	protected abstract void initDimensionUsages();
	
	protected abstract void initMeasures();
	
	protected final void initUserDefinedDimensionUsages() {
		createUserDefinedDimensionUsages(unit, dimensionUsages);
	}

	protected void addDatabaseTable(RolapTable table) {
		databaseTables.add(table);
	}
	
	protected void setDatabaseFactTable(FactTable table) {
		if ( databaseFactTable != null ) {
			throw new IllegalStateException("Fact table set more than once");
		}
		databaseFactTable = table;
		addDatabaseTable(table);
	}
	
	protected void setMondrianTable(MondrianDef.Table table) {
		mondrianTable = table;
	}
	
	protected FactTable getDatabaseFactTable() {
		return databaseFactTable;
	};

	protected void addDimensionUsage(DimensionUsage dim) {
		dimensionUsages.add(dim);
	}

	protected void addMeasure(Measure m) {
		measures.add(m);
	}
	
	protected final void initUserDefinedMeasures() {
		Collection<VariableMetadata> vars = unit.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if( var.isForAnalysis() && var.isNumeric() ) {
				String variableName = var.getVariableName();
				Measure m = mdf.createMeasure(variableName, var.getVariableLabel());
				addMeasure(m);
			}
		}
	}

	/**
	 * Recursively get dimensions derived from categorical variables
	 * marked forAnalysis.  
	 * 
	 * @param unit
	 * @return
	 */
	private void createUserDefinedDimensionUsages(ObservationUnitMetadata unit, List<DimensionUsage> dims) {
		ObservationUnitMetadata parentUnit = unit.getObsUnitParent();
		if ( parentUnit != null ) {
			createUserDefinedDimensionUsages(parentUnit, dims);
		}
		Collection<VariableMetadata> vars = unit.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if( var.isForAnalysis() && var.isCategorical() ) {
				String varName = var.getVariableName();
				// TODO add _id to foreign key names
				DimensionUsage dim = mdf.createDimensionUsage(varName, varName, varName);
				addDimensionUsage(dim);
			}
		}
	}

	protected RolapSchemaGenerator getSchemaGenerator() {
		return schemaGenerator;
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return unit;
	}
}
