package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.Tables.*;

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
	
	private static final GroundPlotView G = GROUND_PLOT_VIEW;
	public final TableField<Record, Object> PLOT_LOCATION = 
			createField(G.PLOT_LOCATION, new GeodeticCoordinateDataType());
	public final TableField<Record, Object> PLOT_ACTUAL_LOCATION = 
			createField(G.PLOT_ACTUAL_LOCATION, new GeodeticCoordinateDataType());
	public final TableField<Record, Object> PLOT_GPS_READING = 
			createField(G.PLOT_GPS_READING, new GeodeticCoordinateDataType());
	
	PlotFactTable(String schema, ObservationUnitMetadata unit, List<String> measures, List<String> dimensions) {
		super(schema, unit.getFactTableName(), unit, measures, dimensions);
	}
}
