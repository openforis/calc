package org.openforis.calc.psql;

import org.postgis.PGgeometry;
import org.postgis.Point;

/**
 * 
 * @author G. Miceli
 * 
 */
public class GeodeticCoordinate extends PGgeometry {

	private static final long serialVersionUID = 1L;
	private static final int LATLOG_SRID = 4326;

	public static final int SQL_TYPE_CODE = 1111;
	public static final String SQL_TYPE_NAME = "Geometry(Point,4326)";
	
	public GeodeticCoordinate() {
		super();
	}

	public GeodeticCoordinate(double x, double y) {
		super(createPoint(x, y));
	}

	private static Point createPoint(double x, double y) {
		Point point = new Point(x, y);
		point.setSrid(LATLOG_SRID);
		return point;
	}

	public double getLatitude() {
		return ((Point) getGeometry()).y;
	}

	public double getLongitude() {
		return ((Point) getGeometry()).x;
	}
}
