/**
 * 
 */
package org.openforis.calc.collect;

import java.sql.Connection;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.TableField;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.SchemaImpl;
import org.jooq.impl.TableImpl;
import org.openforis.calc.engine.Task;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.AbstractTable;
import org.openforis.calc.schema.InputSchema;
import org.openforis.calc.schema.InputTable;
import org.openforis.calc.schema.Schemas;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * @author S. Ricci
 *
 */
public class CollectInputSchemaCreatorTask extends Task {

	private static final String VIEW_SUFFIX = "_view";
	
	@Autowired
	private Configuration config;
	
	@Override
	public String getName() {
		return "Create relational schema";
	}
	
	@Override
	protected long countTotalItems() {
		return 4;
	}
	
	@Override
	protected void execute() throws Throwable {
		dropInputSchema();
		
		createInputSchema();
		
		addUserDefinedVariableColumns();
		
		addViews();
	}

	private void dropInputSchema() {
		String inputSchemaName = getWorkspace().getInputSchema();
		psql()
			.dropSchemaIfExists(new SchemaImpl(inputSchemaName))
			.cascade()
			.execute();
		incrementItemsProcessed();
	}

	private RelationalSchema createInputSchema() throws CollectRdbException {
		String inputSchemaName = getWorkspace().getInputSchema();
		psql()
			.createSchema(new SchemaImpl(inputSchemaName))
			.execute();
		DataSourceConnectionProvider connectionProvider = (DataSourceConnectionProvider) config.connectionProvider();
		DataSource dataSource = connectionProvider.dataSource();
		RelationalSchema schema = ((CollectJob) getJob()).createInputRelationalSchema();
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		relationalSchemaCreator.createRelationalSchema(schema, connection);
		incrementItemsProcessed();
		return schema;
	}
	
	private void addUserDefinedVariableColumns() {
		Workspace ws = getWorkspace();
		for (Variable<?> v : ws.getUserDefinedVariables()) {
			Entity entity = v.getEntity();
			
			InputSchemaTable table = new InputSchemaTable(entity.getDataTable(), ws.getInputSchema());
			TableField<Record, ?> field = table.getOrCreateField(v.getInputValueColumn(), Psql.DOUBLE_PRECISION);
			
			psql()
				.alterTable(table)
				.addColumn(field)
				.execute();
		}
		incrementItemsProcessed();
	}

	private void addViews() {
		Workspace ws = getWorkspace();
		List<Entity> entities = ws.getEntities();
		for (Entity entity : entities) {
			addView(entity);
		}
		incrementItemsProcessed();
	}

	private void addView(Entity entity) {
		Workspace ws = getWorkspace();
		
		Schemas schemas = new Schemas(ws);
		InputSchema inputSchema = schemas.getInputSchema();
		
		InputTable table = inputSchema.getDataTable(entity);
		
		//create inner select
		SelectQuery<Record> select = psql().selectQuery();
		select.addFrom(table);
		select.addSelect(table.fields());
		
		//for every ancestor, add join condition and select fields
		Entity currentEntity = entity;
		while ( currentEntity.getParent() != null ) {
			InputTable currentTable = inputSchema.getDataTable(currentEntity);
			Entity parentEntity = currentEntity.getParent();
			InputTable parentTable = inputSchema.getDataTable(parentEntity);
			
			select.addJoin(parentTable, 
					currentTable.getParentIdField().eq(parentTable.getIdField()));
			
			addUniqueNameFields(select, parentTable.fields());
			currentEntity = parentEntity;
		}
		
		String viewName = entity.getDataTable() + VIEW_SUFFIX;
		psql()
			.createView(ws.getInputSchema(), viewName)
			.as(select)
			.execute();
	}
	
	private void addUniqueNameFields(SelectQuery<Record> select, Field<?>[] fields) {
		for (Field<?> field : fields) {
			Field<?> existingField = getFieldByName(select, field.getName());
			if ( existingField == null ) {
				select.addSelect(field);
			}
		}
	}

	private Field<?> getFieldByName(SelectQuery<Record> select, String name) {
		for (Field<?> field : select.getSelect()) {
			if ( field.getName().equals(name) ) {
				return field;
			}
		}
		return null;
	}

	class InputSchemaTable extends AbstractTable {

		private static final long serialVersionUID = 1L;
		
		protected InputSchemaTable(String name, String schema) {
			super(name, new SchemaImpl(schema));
		}
		
		@SuppressWarnings("unchecked")
		<R extends Record, T> TableField<R, T>  getOrCreateField(String name, DataType<T> type) {
			Field<?> field = ((TableImpl<R>) this).field(name);
			if ( field == null ) {
				field = createField(name, type, this);
			}
			return (TableField<R, T>) field;
		}
		
		
	}
	
}
