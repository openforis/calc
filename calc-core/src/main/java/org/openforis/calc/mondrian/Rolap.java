/**
 * 
 */
package org.openforis.calc.mondrian;

import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.mondrian.Hierarchy.Level;
import org.openforis.calc.mondrian.Schema.Cube;
import org.openforis.calc.mondrian.Schema.Cube.Measure;
import org.openforis.calc.mondrian.Schema.VirtualCube;
import org.openforis.calc.mondrian.Schema.VirtualCube.CubeUsages.CubeUsage;
import org.openforis.calc.mondrian.Schema.VirtualCube.VirtualCubeDimension;
import org.openforis.calc.mondrian.Schema.VirtualCube.VirtualCubeMeasure;
import org.openforis.calc.mondrian.Table.AggName;
import org.openforis.calc.mondrian.Table.AggName.AggForeignKey;
import org.openforis.calc.mondrian.Table.AggName.AggLevel;
import org.openforis.calc.mondrian.Table.AggName.AggMeasure;

/**
 * @author M. Togna
 * 
 */
public class Rolap {

	public static final String MEASURES = "Measures";

	public static final String DIMENSION_TYPE_STANDARD = "StandardDimension";
	public static final String NUMBER_FORMAT_STRING = "###,##0.####";
	public static final String DATA_TYPE_NUMERIC = "Numeric";

	public static AggLevel createAggLevel(String hierarchy, String name, String column) {
		AggLevel aggLevel = new AggLevel();
		aggLevel.setColumn(column);
		aggLevel.setName(toMdx(hierarchy, name));
		return aggLevel;
	}

	public static AggMeasure createAggMeasure(String name, String column) {
		AggMeasure aggMeasure = new AggMeasure();
		aggMeasure.setColumn(column);
		String aggMeasureName = getMdxMeasureName(name);
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

	public static SharedDimension createSharedDimension(String name, String table, String schema, String column, String nameColumn, String caption, String dimCaption) {
		SharedDimension dim = new SharedDimension();
		dim.setType(DIMENSION_TYPE_STANDARD);
		dim.setName(name);
		if( dimCaption!= null){
			dim.setCaption(dimCaption);
		}

		Hierarchy h = new Hierarchy();
		h.setName(name);
		h.setHasAll(true);

		Table t = createTable(schema, table);
		h.setTable(t);

		Level l = createLevel(table, name, column, nameColumn, caption);
		h.getLevel().add(l);

		dim.getHierarchy().add(h);
		return dim;
	}


	public static View createSqlView(String alias, String sql) {
		View v = new View();
		v.setAlias(alias);
		SQL s = new SQL();
		s.setContent(sql);
		v.getSQL().add(s);
		return v;
	}

	public static Level createLevel( String name, String column, String nameColumn , String caption ) {
		return createLevel(null, name, column, nameColumn, caption);
	}

	public static Level createLevel( String table, String name, String column, String nameColumn , String caption ) {
		Level l = new Level();
		if( StringUtils.isNotBlank(table) ){
			l.setTable(table);
		}
		l.setName(name);
		l.setColumn(column);
		l.setNameColumn(nameColumn);
		l.setCaption(caption);
		l.setType("Integer");
		return l;
	}
	
	
	// =====================================
	// virtual cube methods
	// =====================================
	public static VirtualCube createVirtualCube(String name) {
		VirtualCube cube = new VirtualCube();
		cube.setEnabled(true);
		cube.setName(name);
		return cube;
	}
	
	public static CubeUsage createCubeUsage( String name ){
		CubeUsage cubeUsage = new CubeUsage();
		cubeUsage.setCubeName(name);
		cubeUsage.setIgnoreUnrelatedDimensions( true );
		return cubeUsage;
	}
	
	public static VirtualCubeDimension createVirtualCubeDimension( String cubeName, String name ){
		VirtualCubeDimension virtualCubeDimension = new VirtualCubeDimension();
		virtualCubeDimension.setCubeName( cubeName );
		virtualCubeDimension.setName( name );
		return virtualCubeDimension;
	}
	
	public static VirtualCubeMeasure createVirtualCubeMeasure(String cubeName, String name, boolean visible) {
		VirtualCubeMeasure m = new VirtualCubeMeasure();
		m.setCubeName(cubeName);
		m.setName( getMdxMeasureName(name) );
		m.setVisible(visible);
		
		return m;
	}
	
	public static CalculatedMember createCalculatedMember( String dimension, String name, String caption, String formula , boolean visible ) {
		CalculatedMember m = new CalculatedMember();
		m.setName( name );
		m.setCaption( caption );
		m.setVisible( visible );
		m.setFormula( formula );
		m.setDimension( dimension );
		return m;
	}

	// =====================================
	// utility methods
	// =====================================
	public static String validMeasure(String value) {
		StringBuilder s = new StringBuilder();
		s.append( "ValidMeasure(" );
		s.append( value );
		s.append( ")" );
		return s.toString();
	}

	public static String getMdxMeasureName(String name) {
		if( name.startsWith("[" + MEASURES + "]") ){
			return name ;
		} else {
			return toMdx(MEASURES, name);
		}
	}

	public static String toMdx(String prefix, String postfix) {
		return "[" + prefix + "]" + ".[" + postfix + "]";
	}
}
