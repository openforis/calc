/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.jooq.Field;
import org.jooq.Select;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * @author S. Ricci
 *
 */
@Repository
public class InputSchemaDao {

	@Autowired
	private DataSource dataSource;

	public void createViews(Workspace ws) {
		for (Entity entity : ws.getEntities()) {
			createView(entity);
		}
	}
	
	public void createView(Entity entity) {
		Workspace ws = entity.getWorkspace();
		
		Schemas schemas = new Schemas(ws);
		InputSchema inputSchema = schemas.getInputSchema();
		EntityDataView view = inputSchema.getDataView(entity);
		Select<?> select = view.getSelect();
		
		new Psql(dataSource)
			.dropViewIfExists(view)
			.execute();
		
		new Psql(dataSource)
			.createView(view)
			.as(select)
			.execute();
	}
	
	public void addUserDefinedVariableColumn(QuantitativeVariable v) {
		Entity entity = v.getEntity();
		Workspace ws = entity.getWorkspace();
		
		Schemas schemas = new Schemas(ws);
		InputSchema inputSchema = schemas.getInputSchema();
		InputTable table = inputSchema.getDataTable(entity);

		Field<BigDecimal> field = table.getQuantityField(v);
		
		new Psql(dataSource)
			.alterTable(table)
			.addColumn(field)
			.execute();
	}
}
