/**
 * 
 */
package org.openforis.calc.psql;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultDataType;

/**
 * Date Postgtres data type jooq class
 * 
 * @author Mino Togna
 * 
 */
public class LocalDateDataType extends DefaultDataType<LocalDate> {

	private static final long				serialVersionUID	= 1L;

	private static final DateTimeFormatter	FORMATTER			= DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final String				SQL_TYPE_NAME		= "date";

	public LocalDateDataType() {
		super(SQLDialect.POSTGRES, LocalDate.class, SQL_TYPE_NAME);
	}

	@Override
	public LocalDate convert(Object object) {
		if (object instanceof String) {

			return LocalDate.parse(object.toString(), FORMATTER);
		}
		return super.convert(object);
	}

}
