package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.rolap.RolapSchemaGenerator.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public abstract class CubeGenerator {
	
	private static final String MDX_NUMERIC = "Numeric";
	private static final String MDX_SUM_AGGREGATOR = "sum";
//	private static final String FORMAT_STRING_0_DECIMAL = "#,###";
	private static final String FORMAT_STRING_5_DECIMAL = "#,###.#####";
	// IN
	private String dbSchema;
	private ObservationUnitMetadata unit;
	
	// OUT
	private Cube cube;
	private List<RolapTable> tables;
	
	// TEMP
	private FactTable factTable;
	
	CubeGenerator(String dbSchema, ObservationUnitMetadata unit) {
		this.dbSchema = dbSchema;
		this.unit = unit;
	}
	
	Cube createCube() {
		tables = new ArrayList<RolapTable>();

		initCube();
		initFactTable();
		initAggregateTables();
		
		return cube;
	}

	List<RolapTable> getDatabaseTables() {
		return tables;
	}
	
	public String getDatabaseSchema() {
		return dbSchema;
	}
	
	public static CubeGenerator createInstance(String dbSchema, ObservationUnitMetadata unit) {
		Type unitType = unit.getObsUnitTypeEnum();
		switch (unitType) {
		case PLOT:
			return new PlotCubeGenerator(dbSchema, unit);
		case SPECIMEN:
			return new SpecimenCubeGenerator(dbSchema, unit);
		case INTERVIEW:
			return new InterviewCubeGenerator(dbSchema, unit);
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private void initCube() {
		cube = new Cube();
		cube.name = toMdxName(unit.getObsUnitName());
		cube.cache = true;
		cube.enabled = true;
		cube.visible = true;
	}

	private void initFactTable() {
		List<DimensionUsage> dimUsages = getDimensionUsages();
		List<Measure> measures = getMeasures();		
		List<String> dimColumns = extractDimensionColumns(dimUsages);
		List<String> measureColumns = extractMeasureColumns(measures);
				
		factTable = createFactTable(measureColumns, dimColumns);
		
		cube.fact = new Table();
		
		addTable(factTable);
	}

	protected abstract FactTable createFactTable(List<String> measureColumns, List<String> dimColumns);

	private static List<String> extractDimensionColumns(List<DimensionUsage> dimUsages) {
		List<String> dimColumns = new ArrayList<String>();
		for (DimensionUsage dim : dimUsages) {
			dimColumns.add( dim.foreignKey );
		}
		return dimColumns;
	}

	private static List<String> extractMeasureColumns(List<Measure> measures) {
		List<String> cols = new ArrayList<String>();
		for (Measure m : measures) {
			cols.add( m.column );
		}
		return cols;
	}

	protected void addTable(RolapTable table) {
		tables.add(table);
	}

	protected List<DimensionUsage> getDimensionUsages() {
		return getVariableDimensions(unit);
	}
	
	protected List<Measure> getMeasures() {
		List<Measure> measures = new ArrayList<Measure>();

		Collection<VariableMetadata> vars = unit.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if( var.isForAnalysis() && var.isNumeric() ) {
				Measure m = createVariableMeasure(var);
				measures.add(m);
			}
		}
		return measures;
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
	private static List<DimensionUsage> getVariableDimensions(ObservationUnitMetadata unit) {
		List<DimensionUsage> dims = new ArrayList<DimensionUsage>();
		ObservationUnitMetadata parentUnit = unit.getObsUnitParent();
		if ( parentUnit != null ) {
			dims.addAll( getVariableDimensions(parentUnit) );
		}
		Collection<VariableMetadata> vars = unit.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if( var.isForAnalysis() && var.isCategorical() ) {
				DimensionUsage dim = createVariableDimensionUsage(var);
				dims.add(dim);
			}
		}
		return dims;
	}

	private static DimensionUsage createVariableDimensionUsage(VariableMetadata var) {
		String source = RolapSchemaGenerator.getVariableDimensionName(var);
		String varName = var.getVariableName();
		return createDimensionUsage(source, varName);
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


	public ObservationUnitMetadata getObservationUnitMetadata() {
		return unit;
	}

	protected FactTable getFactTable() {
		return factTable;
	}
	
	protected void initAggregateTables() {
		// Implement in subclasses if needed
	}
}
