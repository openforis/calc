/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import org.jooq.impl.UpdatableRecordImpl;

/**
 * @author M. Togna
 *
 */
public class FactRecord extends UpdatableRecordImpl<FactRecord> {

	private static final long serialVersionUID = 1L;
	
	public FactRecord(FactTable factTable) {
		super(factTable);
	}

}
