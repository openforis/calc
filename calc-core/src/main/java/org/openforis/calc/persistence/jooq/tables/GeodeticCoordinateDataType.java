/**
 * 
 */
package org.openforis.calc.persistence.jooq.tables;

import org.jooq.SQLDialect;
import org.jooq.impl.AbstractDataType;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.geospatial.GeodeticCoordinate;

/**
 * @author M. Togna
 * 
 */
public class GeodeticCoordinateDataType extends AbstractDataType<Object> {

	private static final long serialVersionUID = 1L;

	

	public GeodeticCoordinateDataType() {
		super(SQLDialect.POSTGRES, SQLDataType.OTHER, GeodeticCoordinate.class, GeodeticCoordinate.SQL_TYPE_NAME);
	}
	
	@Override
	public int getSQLType() {
		return GeodeticCoordinate.SQL_TYPE_CODE;
	}

}
