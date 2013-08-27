/**
 * 
 */
package org.openforis.calc.psql;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDataType;

/**
 * @author G. Miceli	
 * @author M. Togna
 *
 */
public class GeodeticCoordinateDataType extends DefaultDataType<GeodeticCoordinate> {

	private static final long serialVersionUID = 1L;

	public GeodeticCoordinateDataType() {
		super(SQLDialect.POSTGRES, GeodeticCoordinate.class, GeodeticCoordinate.SQL_TYPE_NAME);
	}

}
