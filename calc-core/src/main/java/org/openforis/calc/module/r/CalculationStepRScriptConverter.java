/**
 * 
 */
package org.openforis.calc.module.r;

import javax.sql.DataSource;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.schema.EntityDataView;
import org.openforis.calc.schema.Schemas;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class is responsible for converting one or more calculation step to an R
 * script
 * 
 * @author Mino Togna
 * 
 */
// @Component
public class CalculationStepRScriptConverter {

	@Autowired
	private DataSource dataSource;

	private StringBuilder sb;

	private Workspace workspace;
	private Schemas schemas;
	
	public CalculationStepRScriptConverter(Workspace workspace) {
		this.sb = new StringBuilder();
		this.workspace = workspace;
		this.schemas = new Schemas(workspace);
	}

	public String toRScript(Entity entity, CalculationStep... steps) {
		appendLibrary();
		appendOpenConnection();
		appendSteps(entity, steps);

		return sb.toString();
	}

	/**
	 * Append library declarations
	 */
	private void appendLibrary() {
		sb.append("library('lmfor');");
		sb.append("library('RPostgreSQL');");
	}

	// driver <- dbDriver('PostgreSQL');
	// con <- dbConnect(driver, host="localhost", dbname="calc", user="calc",
	// password="calc", port=5432)
	// dbSendQuery(conn=con, statement= "SET search_path TO naforma1, public");

	//TODO get the database connection properties from file
	private void appendOpenConnection() {
		sb.append("driver <- dbDriver('PostgreSQL');");
		sb.append("con <- dbConnect(driver, host='localhost', dbname='calc', user='calc', password='calc', port=5432)");
		sb.append("dbSendQuery(conn=con, statement='SET search_path TO "
				+ schemas.getInputSchema().getName() + ", public');");
	}

	private void appendSteps(Entity entity, CalculationStep[] steps) {
		EntityDataView view = schemas.getInputSchema().getDataView(entity);
		Field<?> primaryKey = view.getPrimaryKey().getFields().get(0);
		sb.append("data <- dbGetQuery( conn=con , statement=");
		// append the select 
		sb.append("select " );
		sb.append(primaryKey.getName());
		for (CalculationStep step : steps) {
			
		}
	}

}
