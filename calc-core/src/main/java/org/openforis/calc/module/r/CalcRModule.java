package org.openforis.calc.module.r;

import org.openforis.calc.module.Module;

/**
 * Provides support for R-based operations.
 * 
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class CalcRModule extends Module {

	public static final String MODULE_NAME = "calc-r";
	public static final String VERSION_1 = "1.0";

	public CalcRModule() {
		super(MODULE_NAME, VERSION_1);
		registerOperation(new CustomROperation(this));
	}
}