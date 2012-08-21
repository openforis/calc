/**
 * 
 */
package org.openforis.calc.web.viewmodel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Mino Togna
 * 
 */
abstract class AbstractViewModel {

	private Log log;

	public AbstractViewModel() {
		log = LogFactory.getLog(getClass());
	}

	void error(String msg, Throwable t) {
		if ( log.isErrorEnabled() ) {
			log.error(msg, t);
		}
	}

	void info(String msg) {
		if ( log.isInfoEnabled() ) {
			log.info(msg);
		}
	}

	void debug(String msg) {
		if ( log.isDebugEnabled() ) {
			log.debug(msg);
		}
	}

}
