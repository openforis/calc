package org.openforis.calc.geospatial;

import java.awt.geom.Point2D;

import org.postgis.PGgeometry;
import org.postgis.Point;

/**
 * 
 * @author G. Miceli
 *
 */
public class GeodeticCoordinate extends Point {

	private static final long serialVersionUID = 1L;
	private static final int LATLOG_SRID = 4326;

	public GeodeticCoordinate() {
		super();
	}

	public GeodeticCoordinate(double x, double y, int srid) {
		super(x, y);
		setSrid(srid);
	}

	public static GeodeticCoordinate toInstance(double x, double y, String srsId) {
		Point2D pos = TransformationUtils.toLatLong(x, y, srsId);
		if ( pos == null ) {
			return null;
		} else {
			return new GeodeticCoordinate(pos.getX(), pos.getY(), LATLOG_SRID);
		}
	}

	public PGgeometry toPGGeometry() {
		return new PGgeometry(this);
	}
}
