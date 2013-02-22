package org.openforis.calc.transformation;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.CLUSTER;
import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION_VIEW;
import static org.openforis.calc.persistence.jooq.Tables.STRATUM;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.jooq.tables.Aoi;
import org.openforis.calc.persistence.jooq.tables.Cluster;
import org.openforis.calc.persistence.jooq.tables.PlotSectionView;
import org.openforis.calc.persistence.jooq.tables.Stratum;

/**
 * 
 * @author M. Togna
 * 
 */
public class DataType {

	private Collection<VariableMetadata> variables;
	private static final Stratum S = STRATUM;
	private static final Aoi A = AOI;
	private static final Cluster C = CLUSTER;
	private static final PlotSectionView P = PLOT_SECTION_VIEW;

	static final String TYPE_GEOMETRY_POINT = "geometry(Point, 4326)";
	static final String TYPE_NUMBER_PRECISION = "number";
	static final String TYPE_INTEGER = "integer";

	private static Map<String, String> DATA_TYPES;

	static {
		DATA_TYPES = new HashMap<String, String>();
		DATA_TYPES.put(DataType.S.STRATUM_ID.getName(), TYPE_INTEGER);
		DATA_TYPES.put(A.AOI_ID.getName(), TYPE_INTEGER);
		DATA_TYPES.put(C.CLUSTER_ID.getName(), TYPE_INTEGER);
		DATA_TYPES.put(P.PLOT_SECTION_ID.getName(), TYPE_INTEGER);

		DATA_TYPES.put("cnt", TYPE_INTEGER);
		DATA_TYPES.put("est_area", TYPE_NUMBER_PRECISION);

		DATA_TYPES.put(P.PLOT_GPS_READING.getName(), TYPE_GEOMETRY_POINT);
		DATA_TYPES.put(P.PLOT_ACTUAL_LOCATION.getName(), TYPE_GEOMETRY_POINT);
		DATA_TYPES.put(P.PLOT_LOCATION.getName(), TYPE_GEOMETRY_POINT);
		DATA_TYPES.put(P.PLOT_LOCATION_DEVIATION.getName(), TYPE_NUMBER_PRECISION);
	}

	public DataType(Collection<VariableMetadata> variableMetadata) {
		this.variables = variableMetadata;
	}

	boolean isInteger(String name) {
		return getDataType(name).equals(TYPE_INTEGER);
	}

	boolean isDoublePrecision(String name) {
		return getDataType(name).equals(TYPE_NUMBER_PRECISION);
	}

	boolean isGeometryPoint(String name) {
		return getDataType(name).equals(TYPE_GEOMETRY_POINT);
	}

	public String getDataType(String name) {
		String dataType = null;
		if ( DATA_TYPES.containsKey(name) ) {
			dataType = DATA_TYPES.get(name);
		} else {
			for ( VariableMetadata var : variables ) {
				if ( var.getVariableName().equals(name) ) {
					dataType = TYPE_INTEGER;
				}
			}
		}
		if ( dataType == null ) {
			dataType = TYPE_NUMBER_PRECISION;
		}
		return dataType;
	}

}
