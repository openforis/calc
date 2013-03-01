package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.GeodeticCoordinateDataType;
import org.openforis.calc.persistence.jooq.tables.GroundPlotView;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class PlotFactTable extends FactTable {

	private static final long serialVersionUID = 1L;
	
	// Fixed dimensions
	public final TableField<Record, Integer> PLOT_ID = 
			createFixedDimensionField("plot_id");
	public final TableField<Record, Integer> STRATUM_ID = 
			createFixedDimensionField("stratum_id");
	
	// Fixed measure
	public final TableField<Record, BigDecimal> EST_AREA = 
			createFixedMeasureField("est_area");
	public final TableField<Record, BigDecimal> PLOT_LOCATION_DEVIATION = 
			createFixedMeasureField(G.PLOT_LOCATION_DEVIATION.getName());

	// Plot coordinates
	private static final GroundPlotView G = GROUND_PLOT_VIEW;
	public final TableField<Record, Object> PLOT_LOCATION = 
			createField(G.PLOT_LOCATION, new GeodeticCoordinateDataType());
	public final TableField<Record, Object> PLOT_ACTUAL_LOCATION = 
			createField(G.PLOT_ACTUAL_LOCATION, new GeodeticCoordinateDataType());
	public final TableField<Record, Object> PLOT_GPS_READING = 
			createField(G.PLOT_GPS_READING, new GeodeticCoordinateDataType());

	private List<TableField<Record, Integer>> aoiFields;
	
	PlotFactTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getFactTableName(), unit);
		initFields();
	}
	
	protected void initFields() {
		aoiFields = createAoiFields();
		initUserDefinedFields();
	}

	public List<TableField<Record, Integer>> getAoiFields() {
		return Collections.unmodifiableList(aoiFields);
	}
}
