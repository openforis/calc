package org.openforis.calc.module.sql;

import org.openforis.calc.module.Module;

/**
 Provides support for SQL-based operations.
 * 
 * @author G. Miceli
 * @author M. Togna
 * @author S. Ricci
 */
public class CalcSqlModule extends Module {
	
	public static final String MODULE_NAME = "calc-sql";

	public CalcSqlModule() {
		super(MODULE_NAME, "1.0");
		registerOperation(new CustomSqlOperation(this));
	}

}