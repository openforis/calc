package org.openforis.calc.persistence.jooq.rolap;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import mondrian.olap.MondrianDef.Schema;

import org.eigenbase.xom.XMLOutput;

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

	public void saveMondrianSchemaXml(String filename) throws IOException {
		XMLOutput out = new XMLOutput(new FileWriter(filename));
		out.setAlwaysQuoteCData(true);
		mondrianSchema.displayXML(out, 1);
		
	}
}