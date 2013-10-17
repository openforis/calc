/**
 * 
 */
package org.openforis.calc.collect;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
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
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * @author S. Ricci
 *
 */
public class CollectInputSchemaCreatorTask extends Task {

	@Autowired
	private Configuration config;
	
	@Override
	public String getName() {
		return "Create relational schema";
	}
	
	@Override
	protected void execute() throws Throwable {
		dropInputSchema();
		
		createInputSchema();
		
		addUserDefinedVariableColumns();
	}

	private void dropInputSchema() {
		String inputSchemaName = getWorkspace().getInputSchema();
		psql()
			.dropSchemaIfExists(new SchemaImpl(inputSchemaName))
			.cascade()
			.execute();
	}

	private RelationalSchema createInputSchema() throws CollectRdbException {
		String inputSchemaName = getWorkspace().getInputSchema();
		psql()
			.createSchema(new SchemaImpl(inputSchemaName))
			.execute();
		DataSourceConnectionProvider connectionProvider = (DataSourceConnectionProvider) config.connectionProvider();
		DataSource dataSource = connectionProvider.dataSource();
		RelationalSchemaGenerator rdbGenerator = new RelationalSchemaGenerator();
		CollectSurvey survey = ((CollectJob) getJob()).getSurvey();
		RelationalSchema schema = rdbGenerator.generateSchema(survey, inputSchemaName);
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		relationalSchemaCreator.createRelationalSchema(schema, connection);
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
