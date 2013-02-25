/**
 * 
 */
package org.openforis.calc.persistence.jooq.olap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.persistence.jooq.GeodeticCoordinateDataType;

/**
 * @author M. Togna
 * 
 */
public class FactTable extends OlapTable<FactRecord> {

	private static final long serialVersionUID = 1L;

	private List<TableField<FactRecord, Integer>> dimensionFields;
	private List<TableField<FactRecord, BigDecimal>> measureFields;
	private List<TableField<FactRecord, Object>> pointFields;
	// private TableField<FactRecord, Integer> idField;
	private String[] measures;
	private String[] dimensions;
	private String[] points;

	public FactTable(String schema, String table, String[] measures, String[] dimensions, String[] points) {
		super(table, schema);

		measureFields = new ArrayList<TableField<FactRecord, BigDecimal>>();
		pointFields = new ArrayList<TableField<FactRecord, Object>>();
		dimensionFields = new ArrayList<TableField<FactRecord, Integer>>();

		this.dimensions = dimensions;
		this.measures = measures;
		this.points = points;

		// createId();

		setMeasures();
		setDimensions();
		setPoints();
	}

	// private void createId() {
	// idField = createField("id", SQLDataType.INTEGER, this);
	// }

	public FactTable(String schema, String table, String[] measures, String[] dimensions) {
		this(schema, table, measures, dimensions, null);
	}

	private void setPoints() {
		if ( points != null ) {
			for ( String point : points ) {
				// TableField<FactRecord, PGgeometry> field = createField(point, PostgresDataType.getDefaultDataType("USER-DEFINED"), this);
				TableField<FactRecord, Object> field = createField(point, new GeodeticCoordinateDataType(), this);
				pointFields.add(field);
			}
		}

	}

	private void setDimensions() {
		if ( dimensions != null ) {
			for ( String dimension : dimensions ) {
				TableField<FactRecord, Integer> field = createField(dimension, SQLDataType.INTEGER, this);
				dimensionFields.add(field);
			}
		}
	}

	private void setMeasures() {
		if ( measures != null ) {
			for ( String measure : measures ) {
				TableField<FactRecord, BigDecimal> field = createField(measure, SQLDataType.NUMERIC, this);
				measureFields.add(field);
			}
		}
	}

	public List<TableField<FactRecord, Integer>> getDimensionFields() {
		return Collections.unmodifiableList(dimensionFields);
	}

	public List<TableField<FactRecord, BigDecimal>> getMeasureFields() {
		return Collections.unmodifiableList(measureFields);
	}

	public List<TableField<FactRecord, Object>> getPointFields() {
		return Collections.unmodifiableList(pointFields);
	}

	// public TableField<FactRecord, Integer> getIdField() {
	// return idField;
	// }

	public FactTable aggregate(String table, String[] exludedDimensions) {
		return aggregate(table, exludedDimensions, null, null);
	}

	public FactTable aggregate(String table, String[] exludedDimensions, String[] excludedMeasures, String[] excludedPoints) {
		String[] aggDimensions = exclude(dimensions, exludedDimensions);
		String[] aggPoints = exclude(points, excludedPoints);
		String[] aggMeasures = exclude(measures, excludedMeasures);

		return new FactTable(getSchema().getName(), table, aggMeasures, aggDimensions, aggPoints);
	}

	private String[] exclude(String[] array, String[] exclude) {
		if ( ArrayUtils.isEmpty(exclude) ) {
			return array;
		}

		String[] newArray = new String[] {};
		for ( String string : array ) {
			if ( !ArrayUtils.contains(exclude, string) ) {
				newArray = ArrayUtils.add(newArray, string);
			}
		}
		return newArray;
	}

}
