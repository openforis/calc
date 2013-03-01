package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.rolap.RolapSchemaGenerator.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.AggTable;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.CubeDimension;
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
public abstract class CubeGenerator {
	
	private static final String MDX_NUMERIC = "Numeric";
	private static final String MDX_SUM_AGGREGATOR = "sum";
//	private static final String FORMAT_STRING_0_DECIMAL = "#,###";

	private static final String FORMAT_STRING_5_DECIMAL = "#,###.#####";
	// IN
	private String dbSchema;
	private ObservationUnitMetadata unit;
	private RolapSchemaGenerator schemaGenerator;

	// TEMP
	private List<RolapTable> databaseTables;
	private FactTable databaseFactTable;
	private List<DimensionUsage> dimensionUsages;
	private List<Measure> measures;
	private Table mondrianTable;
	private List<AggTable> mondrianAggregateTables;

	// OUT
	private Cube cube;
	
	CubeGenerator(RolapSchemaGenerator schemaGenerator, ObservationUnitMetadata unit) {
		this.schemaGenerator = schemaGenerator;
		this.dbSchema = schemaGenerator.getDatabaseSchema();
		this.unit = unit;
	}
	
	Cube createCube() {
		databaseTables = new ArrayList<RolapTable>();
		
		dimensionUsages = new ArrayList<MondrianDef.DimensionUsage>();
		measures = new ArrayList<MondrianDef.Measure>();
		mondrianAggregateTables = new ArrayList<MondrianDef.AggTable>();
		
		initFactTable();
		initDimensionUsages();
		initMeasures();

		cube = new Cube();
		cube.cache = true;
		cube.enabled = true;
		cube.visible = true;
		cube.name = toMdxName(unit.getObsUnitName());
		cube.fact = mondrianTable;
		mondrianTable.aggTables = mondrianAggregateTables.toArray(new AggTable[0]);
		cube.dimensions = dimensionUsages.toArray(new CubeDimension[0]);
		cube.measures = measures.toArray(new Measure[0]);
		
		return cube;
	}

	List<RolapTable> getDatabaseTables() {
		return databaseTables;
	}
	
	public String getDatabaseSchema() {
		return dbSchema;
	}
	
	public static CubeGenerator createInstance(RolapSchemaGenerator schemaGen, ObservationUnitMetadata unit) {
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
	
//		List<DimensionUsage> dims = createUserDefinedDimensionUsages();
//		// Add AOIs and Fixed Dimensions
//		List<Measure> measures = createUserDefinedMeasures();		

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
//	protected Table getMondrianTable() {
//		return mondrianTable;
//	}
	
	protected void addMondrianAggegateTable(MondrianDef.AggTable table) {
		mondrianAggregateTables.add(table);
	}

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
				Measure m = createVariableMeasure(var);
				addMeasure(m);
			}
		}
	}
	
	private Measure createVariableMeasure(VariableMetadata var) {
		Measure m = createMeasure(var.getVariableName(), var.getVariableLabel());
		// TODO other number formats and aggregators based on metadata
		return m;
	}

	protected static Measure createMeasure(String column, String caption) {
		Measure m = new Measure();
		m.column = column;
		m.name = toMdxName(m.column);
		m.datatype = MDX_NUMERIC;
		m.aggregator = MDX_SUM_AGGREGATOR;
		m.caption = caption;
		m.formatString = FORMAT_STRING_5_DECIMAL;
		m.visible = true;
		return m;
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
				String source = RolapSchemaGenerator.getVariableDimensionName(var);
				String varName = var.getVariableName();
				DimensionUsage dim = createDimensionUsage(source, varName);
				addDimensionUsage(dim);
			}
		}
	}

	protected static DimensionUsage createDimensionUsage(String source, String foreignKey) {
		DimensionUsage dim = new DimensionUsage();
		dim.source = source;
		dim.name = dim.source;
		dim.foreignKey = foreignKey;  // TODO add _id
		dim.visible = true;
		dim.highCardinality = false;
		return dim;
	}

	protected RolapSchemaGenerator getSchemaGenerator() {
		return schemaGenerator;
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return unit;
	}

	protected MondrianDef.Table createMondrianTable(String name) {
		MondrianDef.Table table = new MondrianDef.Table();
		table.schema = getDatabaseSchema();
		table.name = name;
		return table;
	}
}
