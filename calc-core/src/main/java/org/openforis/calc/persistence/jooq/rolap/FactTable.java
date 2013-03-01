/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import static org.jooq.impl.SQLDataType.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.GeodeticCoordinateDataType;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public abstract class FactTable extends RolapTable {

	private static final long serialVersionUID = 1L;

	private List<TableField<Record, Integer>> dimensionFields;
	private List<TableField<Record, BigDecimal>> measureFields;
	private List<TableField<Record, Object>> pointFields;
	
	private List<String> measures;
	private List<String> dimensions;

	public final TableField<Record, Integer> COUNT = createField("cnt", INTEGER, this);

	private ObservationUnitMetadata observationUnitMetadata;
	
	FactTable(String schema, String name, ObservationUnitMetadata unit, List<String> measures, List<String> dimensions) {
		super(schema, name);
		this.observationUnitMetadata = unit;
		
		measureFields = new ArrayList<TableField<Record, BigDecimal>>();
		dimensionFields = new ArrayList<TableField<Record, Integer>>();

		setMeasures(measures);
		setDimensions(dimensions);
	}

	public ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}

	private void setDimensions(List<String> dimensions) {
		this.dimensions = dimensions;
		if ( dimensions != null ) {
			for ( String dimension : dimensions ) {
				if ( getField(dimension) == null ) {
					TableField<Record, Integer> field = createField(dimension, SQLDataType.INTEGER, this);
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
					TableField<Record, BigDecimal> field = createField(measure, SQLDataType.NUMERIC, this);
					measureFields.add(field);
				}
			}
		}
	}

	public List<TableField<Record, Integer>> getDimensionFields() {
		return Collections.unmodifiableList(dimensionFields);
	}

	public List<TableField<Record, BigDecimal>> getMeasureFields() {
		return Collections.unmodifiableList(measureFields);
	}

	public List<TableField<Record, Object>> getPointFields() {
		return Collections.unmodifiableList(pointFields);
	}

	public AggregateTable createAggregateTable(String infix, String... excludedDimensionColumns) {

		List<String> aggDimensions = new ArrayList<String>(dimensions);
		aggDimensions.removeAll(Arrays.asList(excludedDimensionColumns));

		return new AggregateTable(this, infix, measures, aggDimensions);
	}
	
}
