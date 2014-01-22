/**
 * 
 */
package org.openforis.calc.psql;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDataType;

/**
 * Serial Postgtres data type jooq class
 * 
 * @author Mino Togna
 * 
 */
public class SerialDataType extends DefaultDataType<Long> {

	private static final long serialVersionUID = 1L;
	
	public static final String SQL_TYPE_NAME = "serial";

	public SerialDataType() {
		super(SQLDialect.POSTGRES, Long.class, SQL_TYPE_NAME);
	}

}
