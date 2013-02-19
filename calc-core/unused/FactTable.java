/**
 * 
 */
package org.openforis.calc.persistence.jooq.tables;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.UpdatableTableImpl;
import org.jooq.util.postgres.PostgresDataType;
import org.openforis.calc.persistence.jooq.tables.records.FactRecord;

/**
 * @author Mino Togna
 * 
 */
public class FactTable extends UpdatableTableImpl<FactRecord> {

	private static final long serialVersionUID = 1L;

	private List<TableField<FactRecord, Integer>> dimensionFields;
	private List<TableField<FactRecord, BigDecimal>> measureFields;
	private List<TableField<FactRecord, Object>> pointFields;

	public FactTable(String schema, String table, String[] measures, String[] dimensions, String[] points) {
		super(table, new SchemaImpl(schema));

		measureFields = new ArrayList<TableField<FactRecord, BigDecimal>>();
		pointFields = new ArrayList<TableField<FactRecord, Object>>();
		dimensionFields = new ArrayList<TableField<FactRecord, Integer>>();

		setMeasures(measures);
		setDimensions(dimensions);
		setPoints(points);
	}

	public FactTable(String schema, String table, String[] measures, String[] dimensions) {
		this(schema, table, measures, dimensions, null);
	}

	private void setPoints(String[] points) {
		if ( points != null ) {
			for ( String point : points ) {
				TableField<FactRecord, Object> field = createField(point, PostgresDataType.getDefaultDataType("USER-DEFINED"), this);
				pointFields.add(field);
			}
		}

	}

	private void setDimensions(String[] dimensions) {
		if ( dimensions != null ) {
			for ( String dimension : dimensions ) {
				TableField<FactRecord, Integer> field = createField(dimension, SQLDataType.INTEGER, this);
				dimensionFields.add(field);
			}
		}
	}

	private void setMeasures(String[] measures) {
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

	// public void addMeasureField(String name){
	// createField(name, type, table)
	// }

}
