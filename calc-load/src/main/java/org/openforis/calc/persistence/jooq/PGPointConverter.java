package org.openforis.calc.persistence.jooq;

import java.awt.geom.Point2D;

import org.jooq.Converter;
import org.postgresql.geometric.PGpoint;

/**
 * 
 * @author G. Miceli
 * NOT YET SUPPORTED BY jOOQ DUE TO BUT IN CODE GENERATOR
 */
public class PGPointConverter implements Converter<Object, Point2D.Double> {

	private static final long serialVersionUID = 1L;

	@Override
	public Point2D.Double from(Object databaseObject) {
		PGpoint pgpoint = (PGpoint) databaseObject;
		return new Point2D.Double(pgpoint.x, pgpoint.y);
	}

	@Override
	public PGpoint to(Point2D.Double point2d) {
		return new PGpoint(point2d.x, point2d.y);
	}

	@Override
	public Class<Object> fromType() {
		return Object.class;
	}

	@Override
	public Class<Point2D.Double> toType() {
		return Point2D.Double.class;
	}

}
