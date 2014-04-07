/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.impl.EnumConverter;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.Variable.Scale;

/**
 * @author Mino Togna
 * 
 */
public class VariableScaleConverter extends EnumConverter<String, Scale> {

	private static final long serialVersionUID = 1L;

	public VariableScaleConverter() {
		super( String.class, Variable.Scale.class );
	}

}
