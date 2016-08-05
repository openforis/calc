/**
 * 
 */
package org.openforis.calc.system;

import org.openforis.calc.persistence.jooq.tables.pojos.SystemPropertyBase;

/**
 * @author M. Togna
 *
 */
public class SystemProperty extends SystemPropertyBase {
	
	private static final long serialVersionUID = 1L;

	public enum TYPE {
		BOOLEAN, INTEGER, REAL, STRING, DIRECTORY, FILE
	}
	
	public enum PROPERTIES {
		R_EXEC_DIR;
		
		public String toString(){
			return super.toString().toLowerCase();
		}
	}
	
	/**
	 * 
	 */
	public SystemProperty() {
	}

}
