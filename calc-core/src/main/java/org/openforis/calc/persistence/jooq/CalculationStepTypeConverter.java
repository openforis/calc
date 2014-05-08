/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import org.jooq.impl.EnumConverter;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;

/**
 * @author Mino Togna
 * 
 */
public class CalculationStepTypeConverter extends EnumConverter<String, Type> {

	private static final long serialVersionUID = 1L;

	public CalculationStepTypeConverter() {
		super( String.class, CalculationStep.Type.class );
	}

}
