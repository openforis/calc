/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.GeodeticCoordinateDataType;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public class FactTable extends RolapTable<FactRecord> {

	private static final long serialVersionUID = 1L;

	private List<TableField<FactRecord, Integer>> dimensionFields;
	private List<TableField<FactRecord, BigDecimal>> measureFields;
	private List<TableField<FactRecord, Object>> pointFields;
	
	private List<String> measures;
	private List<String> dimensions;

	public final org.jooq.TableField<FactRecord, Integer> COUNT = createField("cnt", org.jooq.impl.SQLDataType.INTEGER, this);

	private ObservationUnitMetadata observationUnitMetadata;
	
	FactTable(String schema, ObservationUnitMetadata unit, List<String> measures, 
			List<String> dimensions, List<String> points) {
		this(schema, unit.getFactTableName(), unit, measures, dimensions, points);
	}

	protected FactTable(String schema, String name, ObservationUnitMetadata unit, List<String> measures,
			List<String> dimensions, List<String> points) {
		super(schema, name, FactRecord.class);
		this.observationUnitMetadata = unit;
		
		measureFields = new ArrayList<TableField<FactRecord, BigDecimal>>();
		pointFields = new ArrayList<TableField<FactRecord, Object>>();
		dimensionFields = new ArrayList<TableField<FactRecord, Integer>>();

		setMeasures(measures);
		setDimensions(dimensions);
		setPoints(points);
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}

	// TODO move to PlotFact as constant fields
	private void setPoints(List<String> points) {
		if ( points != null ) {
			for ( String point : points ) {
				TableField<FactRecord, Object> field = createField(point, new GeodeticCoordinateDataType(), this);
				pointFields.add(field);
			}
		}
	}

	private void setDimensions(List<String> dimensions) {
		this.dimensions = dimensions;
		if ( dimensions != null ) {
			for ( String dimension : dimensions ) {
				if ( getField(dimension) == null ) {
					TableField<FactRecord, Integer> field = createField(dimension, SQLDataType.INTEGER, this);
					dimensionFields.add(field);
				}
			}
		}
	}

	private void setMeasures(List<String> measures) {
		this.measures = measures;
		if ( measures != null ) {
			for ( String measure : measures ) {
				if ( getField(measure) == null ) {
					TableField<FactRecord, BigDecimal> field = createField(measure, SQLDataType.NUMERIC, this);
					measureFields.add(field);
				}
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

	public AggregateTable createAggregateTable(String infix, String... excludedDimensionColumns) {

		List<String> aggDimensions = new ArrayList<String>(dimensions);
		aggDimensions.removeAll(Arrays.asList(excludedDimensionColumns));

		return new AggregateTable(this, infix, measures, aggDimensions);
	}
	
}
