/**
 * 
 */
package org.openforis.calc.collect;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.schema.AbstractTable;

/**
 * @author S. Ricci
 *
 */
public class SpeciesCodeTable extends AbstractTable {

	private static final long serialVersionUID = 1L;
	private TableField<Record, Integer> idField;
	private TableField<Record, String> codeField;
	private TableField<Record, String> scientificNameField;
	
	SpeciesCodeTable(String speciesList, String schema) {
		super(speciesList + "_code", new SchemaImpl(schema));
		
		idField = createField(getName() + "_id", SQLDataType.INTEGER, this);
		codeField = createField(speciesList, SQLDataType.VARCHAR, this);
		scientificNameField = createField(speciesList + "_scientific_name", SQLDataType.VARCHAR, this);
	}

	public TableField<Record, Integer> getIdField() {
		return idField;
	}

	public TableField<Record, String> getCodeField() {
		return codeField;
	}

	public TableField<Record, String> getScientificNameField() {
		return scientificNameField;
	}


}
