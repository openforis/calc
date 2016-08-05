/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.impl.EnumConverter;
import org.openforis.calc.system.SystemProperty.TYPE;

/**
 * @author M. Togna
 *
 */
public class SystemPropertyTypeConverter extends EnumConverter<String, TYPE> {

	private static final long serialVersionUID = 85460301962699561L;

	public SystemPropertyTypeConverter() {
		super(String.class, TYPE.class);
	}

}
