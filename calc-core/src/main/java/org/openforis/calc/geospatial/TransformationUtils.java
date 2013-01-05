/**
 * 
 */
package org.openforis.calc.geospatial;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.openforis.idm.model.Coordinate;
import org.opengis.geometry.coordinate.Position;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public class TransformationUtils {

	private static final String LATLONG_SRS_ID = "EPSG:4326";

	private static final Log LOG = LogFactory.getLog(TransformationUtils.class);

	private static Map<String, MathTransform> TO_LATLONG_TRANSFORMS;
	private static GeodeticCalculator CALCULATOR;

	static {
		init();
	}

	/**
	 * Returns the orthodromic distance between two points
	 * 
	 * @param startingPosition
	 * @param destinationPosition
	 * @return
	 * @throws TransformException
	 */
	public synchronized double orthodromicDistance(Position startingPosition, Position destinationPosition) {
		try {
			CALCULATOR.setStartingPosition(startingPosition);
			CALCULATOR.setDestinationPosition(destinationPosition);
			double result = CALCULATOR.getOrthodromicDistance();
			return result;
		} catch (TransformException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized double orthodromicDistance(double startX, double startY, String startSRSId, double destX, double destY, String destSRSId) {
		Position startingPosition = toLatLong(startX, startY, startSRSId);
		Position destinationPosition = toLatLong(destX, destY, destSRSId);
		return orthodromicDistance(startingPosition, destinationPosition);
	}

	public synchronized double orthodromicDistance(Coordinate startingCoordinate, Coordinate destinationCoordinate) {
		double startX = startingCoordinate.getX();
		double startY = startingCoordinate.getY();
		String startSRSId = startingCoordinate.getSrsId();

		double destX = destinationCoordinate.getX();
		double destY = destinationCoordinate.getY();
		String destSRSId = destinationCoordinate.getSrsId();

		return orthodromicDistance(startX, startY, startSRSId, destX, destY, destSRSId);
	}

	/*
	public void parseSRS(List<SpatialReferenceSystem> list) {
		for (SpatialReferenceSystem srs : list) {
			parseSRS(srs);
		}
	}
	
	public void parseSRS(SpatialReferenceSystem srs) {
		String srsId = srs.getId();
		MathTransform transform = TO_LATLONG_TRANSFORMS.get(srsId);
		if (transform == null) {
			String wkt = srs.getWellKnownText();
			transform = findMathTransform(srsId, wkt);
			TO_LATLONG_TRANSFORMS.put(srsId, transform);
		}
	}
	 */
	public static DirectPosition2D toLatLong(double x, double y, String srsId) {
		try {
			DirectPosition2D src = new DirectPosition2D(x, y);
			if ( LATLONG_SRS_ID.equals(srsId) ) {
				return src;
			}
			MathTransform transform = getMathTransform(srsId);
			DirectPosition2D dest = new DirectPosition2D();
			transform.transform(src, dest);
			return dest;
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error converting point (" + x + ", " + y + ", '" + srsId+"'): "+t.getMessage());
			}
			return null;
		}
	}
	
	private static MathTransform getMathTransform(String srsId) {
		try {
			MathTransform transform = TO_LATLONG_TRANSFORMS.get(srsId);
			if ( transform == null ) {
				CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null);
//				ThreadedEpsgFactory factory = new ThreadedEpsgFactory();
				CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem(srsId);
				// SYSTEMS.put(srsId, crs);
				transform = CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84);
				if ( transform == null ) {
					throw new IllegalArgumentException("Unknown SRS ID: "+srsId);
				}
				TO_LATLONG_TRANSFORMS.put(srsId, transform);
			}
			return transform;
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error while parsing srsid " + srsId, t);
			}
			throw new RuntimeException(t);
		}
	}

	/*
	private static MathTransform findMathTransform(String srsId, String wkt) {
		try {
			CoordinateReferenceSystem crs = parseWKT(wkt);
			// SYSTEMS.put(srsId, crs);
			MathTransform mathTransform = CRS.findMathTransform(crs, WGS84);
			// TO_WGS84_TRANSFORMS.put(srsId, mathTransform);
			return mathTransform;
		} catch (Throwable t) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error while parsing srsid " + srsId, t);
			}
			throw new RuntimeException(t);
		}
	}
*/
	private static void init() {
		try {
			// SYSTEMS = new HashMap<String, CoordinateReferenceSystem>();
			TO_LATLONG_TRANSFORMS = new HashMap<String, MathTransform>();
			CALCULATOR = new GeodeticCalculator();

//			MathTransform wgs84Transform = CRS.findMathTransform(DefaultGeographicCRS.WGS84, DefaultGeographicCRS.WGS84);
//			TO_LATLONG_TRANSFORMS.put(LATLONG_ID, wgs84Transform);
		} catch (Exception e) {
			if (LOG.isErrorEnabled()) {
				LOG.error("Error while initializing CoordinateOperations", e);
			}
			throw new RuntimeException(e);
		}
	}

}