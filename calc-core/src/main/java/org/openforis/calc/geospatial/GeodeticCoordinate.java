package org.openforis.calc.geospatial;

import java.awt.geom.Point2D;

import org.postgis.Geometry;
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
	public static final String SQL_TYPE_NAME = "geometry(Point,4326)";
	
	public GeodeticCoordinate() {
		super();
	}

	public GeodeticCoordinate(double x, double y) {
		super(createPoint(x, y));
	}

	private static Geometry createPoint(double x, double y) {
		Point point = new Point(x, y);
		point.setSrid(LATLOG_SRID);
		return point;
	}

	public static GeodeticCoordinate toInstance(double x, double y, String srsId) {
		Point2D pos = TransformationUtils.toLatLong(x, y, srsId);
		if ( pos == null ) {
			return null;
		} else {
			return new GeodeticCoordinate(pos.getX(), pos.getY());
		}
	}
	
	// public PGgeometry toPGGeometry() {
	// return new PGgeometry(this);
	// }
}
