package org.openforis.calc.persistence.jooq.rolap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mondrian.olap.MondrianDef;
import mondrian.olap.MondrianDef.AggFactCount;
import mondrian.olap.MondrianDef.AggForeignKey;
import mondrian.olap.MondrianDef.AggLevel;
import mondrian.olap.MondrianDef.AggMeasure;
import mondrian.olap.MondrianDef.AggName;
import mondrian.olap.MondrianDef.Cube;
import mondrian.olap.MondrianDef.CubeDimension;
import mondrian.olap.MondrianDef.Dimension;
import mondrian.olap.MondrianDef.DimensionUsage;
import mondrian.olap.MondrianDef.Hierarchy;
import mondrian.olap.MondrianDef.Level;
import mondrian.olap.MondrianDef.Measure;
import mondrian.olap.MondrianDef.Relation;
import mondrian.olap.MondrianDef.Schema;
import mondrian.olap.MondrianDef.Table;
import mondrian.olap.MondrianDef.View;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;

/**
 * 
 * @author G. Miceli
 *
 */
class MondrianDefFactory {
	
//	private static final String GRANT_ACCESS_ALL = "all";
//	private static final String GRANT_ACCESS_NONE = "none";
    private static final String TYPE_STANDARD_DIMENSION = "StandardDimension";
	private static final String MDX_NUMERIC = "Numeric";
	private static final String MDX_SUM_AGGREGATOR = "sum";
//	private static final String FORMAT_STRING_0_DECIMAL = "#,###";

	private static final String FORMAT_STRING_5_DECIMAL = "#,###.##";

	private String databaseSchema;
	
	public MondrianDefFactory(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}
	
	public Schema createSchema(String name, Collection<Dimension> sharedDimensions, Collection<Cube> cubes) {
		Schema schema = new Schema();
		schema.name = toMdxName(name);
		schema.dimensions = sharedDimensions.toArray(new Dimension[0]);
		schema.cubes = cubes.toArray(new Cube[0]);
		return schema;
	}
	
	public Dimension createDimension(String name, String caption, Hierarchy... hierarchies) {
		Dimension dim = new Dimension();
		dim.name = toMdxName(name);
		dim.type = TYPE_STANDARD_DIMENSION;
		dim.visible = true;
		dim.highCardinality = false;
		dim.caption = caption;
		dim.hierarchies = hierarchies;
		return dim;
	}

	public Hierarchy createHierarchy(String name, boolean hasAll, Relation relation, Level... levels) {
		Hierarchy hier = new Hierarchy();
		hier.name = toMdxName(name);
		hier.visible = true;
		hier.hasAll = hasAll;		
		hier.levels = levels;
		hier.relation = relation;
		return hier;
	}

	public Hierarchy createHierarchy(String name, boolean hasAll, Relation relation, Collection<Level> levels) {
		return createHierarchy(name, hasAll, relation, levels.toArray(new Level[0]));
	}
	
	public Hierarchy createHierarchy(String name, boolean hasAll, String table, Collection<Level> levels) {
		return createHierarchy(name, hasAll, table, levels.toArray(new Level[0]));
	}
	
	public Hierarchy createHierarchy(String name,  boolean hasAll, String table, Level... levels) {
		MondrianDef.Table relation = new MondrianDef.Table(databaseSchema, table, null, null);
		
		return createHierarchy(name, hasAll, relation, levels);
	}

	public Level createLevel(String levelName, String table, String column, String nameColumn) {
		Level level = new Level();
		level.name = toMdxName(levelName);
		level.visible = true;
		level.table = table;
		level.column = column; 
		level.nameColumn = nameColumn;
//		level.type = "String";
		level.uniqueMembers = false;
		level.levelType = "Regular";
		level.hideMemberIf = "Never";

		return level;
	}

	public Level createLevel(String levelName, RolapTable table, Field<Integer> idField,
			Field<String> labelField) {
		return createLevel(levelName, table.getName(), idField.getName(), labelField.getName());
	}
	
	public Level createLevel(String levelName, String column, String nameColumn) {
		return createLevel(levelName, null, column, nameColumn);
	}

	public View createJoinView(HierarchicalDimensionTable leafTable, String alias) {
		View view = new MondrianDef.View();
		MondrianDef.SQL mondrianSql = new MondrianDef.SQL();
		mondrianSql.dialect = "generic";
		mondrianSql.cdata = leafTable.getDenormalizedSelectSql();
		view.selects = new MondrianDef.SQL[] {mondrianSql};
		view.alias = alias;
		return view;
	}

	public Cube createCube(String name, Table table, List<DimensionUsage> dimensionUsages, List<Measure> measures) {
		Cube cube = new Cube();
		cube.cache = false; // TODO set to true in prod
		cube.enabled = true;
		cube.visible = true;
		cube.name = toMdxName(name);
		cube.fact = table;
		cube.dimensions = dimensionUsages.toArray(new CubeDimension[0]);
		cube.measures = measures.toArray(new Measure[0]);
		return cube;
	}


	public DimensionUsage createDimensionUsage(String name, String source, String foreignKey) {
		DimensionUsage dim = new DimensionUsage();
		dim.name = toMdxName(name);
		dim.source = toMdxName(source);
		dim.foreignKey = foreignKey;  
		dim.visible = true;
		dim.highCardinality = false;
		return dim;
	}

	public DimensionUsage createDimensionUsage(String source, Field<Integer> field) {
		return createDimensionUsage(source, source, field.getName());
	}
	
	public MondrianDef.Table createTable(String name) {
		MondrianDef.Table table = new MondrianDef.Table();
		table.name = name;
		table.schema = databaseSchema;
		return table;
	}
	
	public Measure createMeasure(String column, String caption) {
		Measure m = new Measure();
		m.name = toMdxName(column);
		m.column = column;
		m.datatype = MDX_NUMERIC;
		m.aggregator = MDX_SUM_AGGREGATOR;
		m.caption = caption;
		m.formatString = FORMAT_STRING_5_DECIMAL;
		m.visible = true;
		return m;
	}

	public Measure createMeasure(Field<BigDecimal> field, String caption) {
		return createMeasure(field.getName(), caption);
	}
	
	public AggName createAggregateName(AggregateTable<?> table) {
		AggName aggName = new AggName();
		aggName.name = table.getName();
		
		//Fact count
		AggFactCount factCount = new AggFactCount();
		factCount.column = table.AGG_COUNT.getName(); 
		aggName.factcount = factCount;

		//Foreign Keys
		initAggForeignKeys(table, aggName);
		
		//Measures
		initAggMeasures(table, aggName);
		
		//Aoi levels
		if( table instanceof AoiAggregateTable ){
			initAoiAggLevels((AoiAggregateTable<?>) table, aggName);
		}
		return aggName;
	}

	private void initAoiAggLevels(AoiAggregateTable<?> table, AggName aggName) {
		List<AggLevel> aggLevels = new ArrayList<AggLevel>();
		
		AoiHierarchyLevelMetadata aggregationLevel = table.getAoiHierarchyLevelMetadata();
		AoiHierarchyMetadata hierachy = aggregationLevel.getAoiHierachyMetadata();
		List<AoiHierarchyLevelMetadata> levels = hierachy.getLevelMetadata();
		for ( AoiHierarchyLevelMetadata level : levels ) {
			AggLevel aggLevel = new AggLevel();
			String levelName = level.getAoiHierarchyLevelName();
			aggLevel.column = levelName;
			aggLevel.name = "["+toMdxName(hierachy.getAoiHierarchyName())+"].["+toMdxName(levelName)+"]";
			
			aggLevels.add(aggLevel);
			
			if(level.equals(aggregationLevel)){
				break;
			}
		}
		aggName.levels = aggLevels.toArray( new AggLevel[0]);
	}

	private void initAggForeignKeys(AggregateTable<?> table, AggName aggName) {
		List<AggForeignKey> foreignKeys = new ArrayList<MondrianDef.AggForeignKey>();
		
		for ( Field<Integer> field : table.getFixedDimensionFields() ) {
			AggForeignKey foreignKey = createAggForeignKey(field);			
			foreignKeys.add(foreignKey);
		}
		for ( Field<Integer> field : table.getUserDefinedDimensionFields() ) {
			AggForeignKey foreignKey = createAggForeignKey(field);			
			foreignKeys.add(foreignKey);
		}
		
		aggName.foreignKeys = foreignKeys.toArray(new AggForeignKey[0]);
	}

	private AggForeignKey createAggForeignKey(Field<Integer> field) {
		String name = field.getName();
		AggForeignKey foreignKey = new AggForeignKey();
		foreignKey.aggColumn = name;
		foreignKey.factColumn = name;
		return foreignKey;
	}

	private void initAggMeasures(AggregateTable<?> table, AggName aggName) {
		List<AggMeasure> measures = new ArrayList<AggMeasure>();
		
		for ( Field<BigDecimal> field : table.getUserDefinedMeasureFields() ) {
			AggMeasure measure = createAggMeasure(field);
			measures.add(measure);
		}
		for ( Field<BigDecimal> field : table.getFixedMeasureFields() ) {
			if(!field.equals(table.AGG_COUNT)){
				AggMeasure measure = createAggMeasure(field);
				measures.add(measure);
			}
		}
		aggName.measures = measures.toArray(new AggMeasure[0]);
	}

	private AggMeasure createAggMeasure(Field<BigDecimal> field) {
		AggMeasure measure = new AggMeasure();
		String name = field.getName();
		measure.column = name;
		measure.name = "[Measures].[" + toMdxName(name) + "]";
		return measure;
	}
	
	public static String toMdxName(String name) {
		StringBuilder sb = new StringBuilder();
		String[] s = name.split("[_\\-]");

		for ( int i = 0 ; i < s.length ; i++ ) {
			String string = s[i];
			sb.append(string.substring(0, 1).toUpperCase());
			sb.append(string.substring(1, string.length()));
		}

		return sb.toString();
	}
}
