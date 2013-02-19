/**
 * 
 */
package org.openforis.calc.persistence.jooq.tables.records;

import org.jooq.impl.UpdatableRecordImpl;
import org.openforis.calc.persistence.jooq.tables.FactTable;

/**
 * @author Mino Togna
 *
 */
public class FactRecord extends UpdatableRecordImpl<FactRecord> {

	private static final long serialVersionUID = 1L;
	
	public FactRecord(FactTable factTable) {
		super(factTable);
	}


}
