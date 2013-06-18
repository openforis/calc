package org.openforis.calc.engine;

/**
 * Base class for jobs which import data or metadata into the Calc database.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class ImportJob extends Job {

	protected ImportJob(Context context) {
		super(context);
	}
	
}