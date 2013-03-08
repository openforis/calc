package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.Tables.GROUND_PLOT_VIEW;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.tables.GroundPlotView;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class PlotFactTable extends FactTable {

	private static final long serialVersionUID = 1L;

	private static final GroundPlotView G = GROUND_PLOT_VIEW;

	// Fixed dimensions
	public final Field<Integer> CLUSTER_ID = createFixedDimensionField(G.CLUSTER_ID);
	public final Field<Integer> PLOT_ID = createFixedDimensionField("plot_id");
	public final Field<Integer> STRATUM_ID = createFixedDimensionField(G.STRATUM_ID);

	// Fixed measures
	// TODO take from user-defined measures?
	public final Field<BigDecimal> EST_AREA = createFixedMeasureField("est_area");
	public final Field<BigDecimal> PLOT_LOCATION_DEVIATION = createFixedMeasureField(G.PLOT_LOCATION_DEVIATION);

	// Plot coordinates
	public final Field<Object> PLOT_LOCATION = createField(G.PLOT_LOCATION);
	public final Field<Object> PLOT_ACTUAL_LOCATION = createField(G.PLOT_ACTUAL_LOCATION);
	public final Field<Object> PLOT_GPS_READING = createField(G.PLOT_GPS_READING);

	private List<Field<Integer>> aoiFields;

	PlotFactTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getFactTableName(), unit);
		initFields();
	}

	protected void initFields() {
		aoiFields = createAoiFields();
		initUserDefinedFields();
	}

	public List<Field<Integer>> getAoiFields() {
		return Collections.unmodifiableList(aoiFields);
	}
}
