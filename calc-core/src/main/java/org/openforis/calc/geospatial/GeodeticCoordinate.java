package org.openforis.calc.geospatial;

import java.awt.geom.Point2D;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
	
	public static GeodeticCoordinate toInstance(double x, double y, String srsId) {
		Point2D pos = TransformationUtils.toLatLong(x, y, srsId);
		if ( pos == null ) {
			return null;
		} else {
			return new GeodeticCoordinate(pos.getX(), pos.getY());
		}
	}
	public static void main(String[] args) throws Exception {
		CoordinateReferenceSystem utm37 = CRS.decode("EPSG:21037");
		CoordinateReferenceSystem wgs84 = DefaultGeographicCRS.WGS84;
		
		DirectPosition2D wgsCoord = TransformationUtils.toLatLong(173210, 9393900, "EPSG:21037");
//		DirectPosition2D utmCoord = new DirectPosition2D(173210, 9393900);
//		DirectPosition2D wgsCoord = new DirectPosition2D();
//		CRS.findMathTransform(utm37, wgs84).transform(utmCoord, wgsCoord);
		DirectPosition2D utmCoord2 = new DirectPosition2D();
//		DirectPosition2D floorWgs = new DirectPosition2D(Math.floor(wgsCoord.x), Math.floor(wgsCoord.y));
		CRS.findMathTransform(wgs84,  utm37).transform(wgsCoord, utmCoord2);
		System.out.printf("%f, %f", utmCoord2.x, utmCoord2.y);
	}
	// public PGgeometry toPGGeometry() {
	// return new PGgeometry(this);
	// }
}
