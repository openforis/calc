package org.openforis.calc.persistence.jooq.rolap;

import java.util.Collections;
import java.util.List;

import mondrian.olap.MondrianDef.Schema;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class RolapSchemaDefinition {
	private Schema mondrianSchema;
	private List<RolapTable> databaseTables;
	
	RolapSchemaDefinition(Schema mondrianSchema, List<RolapTable> tables) {
		this.mondrianSchema = mondrianSchema;
		this.databaseTables = tables;
	}

	public String getMondrianSchemaXml() {
		return mondrianSchema.toXML();
	}

	public String getDatabaseSchema() {
		return databaseTables.get(0).getSchema().getName();
	}
	
	public List<RolapTable> getDatabaseTables() {
		return Collections.unmodifiableList(databaseTables);
	}
}