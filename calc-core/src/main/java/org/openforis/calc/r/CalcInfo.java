/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author M. Togna
 *
 */
public class CalcInfo extends CalcLog {

	/**
	 * @param previous
	 * @param logLevel
	 * @param step
	 * @param message
	 */
	public CalcInfo(RScript previous, String step, RScript message) {
		super(previous, CalcLogLevel.INFO, step, message);
	}

}
