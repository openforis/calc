package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.Tables.*;

import java.util.Arrays;
import java.util.List;

import mondrian.olap.MondrianDef.DimensionUsage;
import mondrian.olap.MondrianDef.Measure;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.tables.GroundPlotView;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PlotCubeGenerator extends CubeGenerator {

//	private static final String[] FIXED_DIM_COLS = new String[] { 
//    	STRATUM.STRATUM_ID.getName(), 
//    	CLUSTER.CLUSTER_ID.getName(), 
//    	PLOT_SECTION.PLOT_SECTION_ID.getName() 
//    };
	
	PlotCubeGenerator(String dbSchema, ObservationUnitMetadata unit) {
		super(dbSchema, unit);
	}

	@Override
	protected List<DimensionUsage> getDimensionUsages() {
		List<DimensionUsage> dims = super.getDimensionUsages();
		// TODO dynamic stratum and AOI dimension names
		dims.add(createDimensionUsage("Stratum", "stratum_id"));
		dims.add(createDimensionUsage("Plot", "plot_id"));
		dims.add(createDimensionUsage("AOI", "country"));
		dims.add(createDimensionUsage("AOI", "region"));
		dims.add(createDimensionUsage("AOI", "district"));
		// TODO
		return dims;
	}
	
	@Override
	protected List<String> getPointColumns() {
		GroundPlotView G = GROUND_PLOT_VIEW;
		return Arrays.asList(
					G.PLOT_ACTUAL_LOCATION.getName(),
					G.PLOT_GPS_READING.getName(),
					G.PLOT_LOCATION.getName());
	}
	
	@Override
	protected List<Measure> getMeasures() {
		GroundPlotView G = GROUND_PLOT_VIEW;
		// TODO use Field objects for column names
		return Arrays.asList(
				createMeasure(G.PLOT_LOCATION_DEVIATION.getName(), "Location deviation"),
				createMeasure("est_area", "Est. area"),
				createMeasure("cnt", "Count")
				);
	}
	
	@Override
	protected void initAggregateTables() {
		FactTable factTable = getFactTable();
		String rootInfix = "district_stratum";
		AggregateTable rootAggTable = factTable.createAggregateTable(rootInfix, "plot_id");
		addTable(rootAggTable);
	}
}
