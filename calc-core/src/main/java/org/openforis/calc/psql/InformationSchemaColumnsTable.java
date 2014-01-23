/**
 * 
 */
package org.openforis.calc.psql;

import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.TableImpl;

/**
 * @author Mino Togna
 *
 */
public class InformationSchemaColumnsTable extends TableImpl<Record> {

	private static final long serialVersionUID = 1L;

	/**
	 * @param name
	 * @param schema
	 */
	public InformationSchemaColumnsTable(String name, Schema schema) {
		super("columns", new SchemaImpl("information_schema"));
	}

}
