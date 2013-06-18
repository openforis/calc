package org.openforis.calc.engine;

/**
 * Creates and populates aggregate tables for a particular fact table, creating
 * two tables for each AOI level (at AOI/stratum level and one at AOI level).
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class AggregateFactTableTask extends Task {

	protected AggregateFactTableTask(Context context) {
		super(context);
	}
}