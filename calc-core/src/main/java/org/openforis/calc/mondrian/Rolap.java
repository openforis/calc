/**
 * 
 */
package org.openforis.calc.mondrian;

import java.math.BigInteger;

import org.openforis.calc.mondrian.Hierarchy.Level;
import org.openforis.calc.mondrian.Schema.Cube;
import org.openforis.calc.mondrian.Schema.Cube.Measure;
import org.openforis.calc.mondrian.Table.AggName;
import org.openforis.calc.mondrian.Table.AggName.AggForeignKey;
import org.openforis.calc.mondrian.Table.AggName.AggLevel;
import org.openforis.calc.mondrian.Table.AggName.AggMeasure;

/**
 * @author M. Togna
 * 
 */
public class Rolap {

	private static final String MEASURES = "Measures";
	
	public static final String DIMENSION_TYPE_STANDARD = "StandardDimension";
	public static final String NUMBER_FORMAT_STRING = "#,###.##";
	public static final String DATA_TYPE_NUMERIC = "Numeric";
	
	public static AggLevel createAggLevel(String name, String column) {
		AggLevel aggLevel = new AggLevel();
		aggLevel.setColumn(column);
		aggLevel.setName(name);
		return aggLevel;
	}

	public static AggMeasure createAggMeasure(String name, String column) {
		AggMeasure aggMeasure = new AggMeasure();
		aggMeasure.setColumn(column);
		String aggMeasureName = "[Measures]." + "[" + name + "]";
		aggMeasure.setName(aggMeasureName);
		return aggMeasure;
	}

	public static AggForeignKey createAggForeignKey(String factColumn, String aggColumn) {
		AggForeignKey aggForeignKey = new AggForeignKey();
		aggForeignKey.setFactColumn(factColumn);
		aggForeignKey.setAggColumn(aggColumn);
		return aggForeignKey;
	}

	public static AggName createAggregateName(String name, int approxRowCnt) {
		AggName aggTable = new AggName();
		aggTable.setName(name);
		aggTable.setApproxRowCount(BigInteger.valueOf(approxRowCnt));

		AggColumnName aggFactCount = new AggColumnName();
		aggFactCount.setColumn("_agg_cnt");
		aggTable.setAggFactCount(aggFactCount);
		return aggTable;
	}

	public static Schema createSchema(String name) {
		Schema schema = new Schema();
		schema.setName(name);
		return schema;
	}

	public static Table createTable(String schema, String table) {
		Table t = new Table();
		t.setName(table);
		t.setSchema(schema);
		return t;
	}

	public static Cube createCube(String name) {
		Cube cube = new Cube();
		cube.setCache(false);
		cube.setEnabled(true);
		cube.setName(name);
		return cube;
	}

	public static Measure createMeasure(String name, String caption, String column, String aggregator, String dataType, String formatString) {
		Measure m = new Measure();
		m.setName(name);
		m.setColumn(column);
		m.setDatatype(dataType);
		m.setFormatString(formatString);
		m.setAggregator(aggregator);
		m.setCaption(caption);
		m.setVisible(true);
		return m;
	}

	public static DimensionUsage createDimensionUsage(String name, String foreignKey) {
		DimensionUsage dim = new DimensionUsage();
		dim.setSource(name);
		dim.setName(name);
		dim.setForeignKey(foreignKey);
		dim.setHighCardinality(false);
		return dim;
	}

	public static SharedDimension createSharedDimension(String name, String table, String schema, String column, String nameColumn) {
		SharedDimension dim = new SharedDimension();
		dim.setType(DIMENSION_TYPE_STANDARD);
		dim.setName(name);

		Hierarchy h = new Hierarchy();
		h.setName(name);
		h.setHasAll(true);

		Table t = createTable(schema, table);
		h.setTable(t);

		Level l = createLevel(name, table, column, nameColumn);
		h.getLevel().add(l);

		dim.getHierarchy().add(h);
		return dim;
	}

	private static Level createLevel(String name, String table, String column, String nameColumn) {
		Level l = new Level();
		l.setName(name);
		l.setTable(table);
		l.setColumn(column);
		l.setNameColumn(nameColumn);
		return l;
	}

	public static View createSqlView(String alias, String sql) {
		View v = new View();
		v.setAlias(alias);
		SQL s = new SQL();
		s.setContent(sql);
		v.getSQL().add(s);
		return v;
	}

	public static Level createLevel(String name, String column, String nameColumn) {
		Level l = new Level();
		l.setName(name);
		l.setColumn(column);
		l.setNameColumn(nameColumn);
		return l;
	}

	public static String getMdxMeasureName(String name) {
		return toMdx(MEASURES , name);
	}

	public static String toMdx(String prefix, String postfix) {
		return "[" + prefix + "]" + ".[" + postfix + "]";
	}
}
