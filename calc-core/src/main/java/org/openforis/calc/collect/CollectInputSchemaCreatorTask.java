/**
 * 
 */
package org.openforis.calc.collect;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.engine.Task;
import org.openforis.calc.psql.Psql.Privilege;
import org.openforis.calc.schema.AbstractTable;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.openforis.collect.relational.model.RelationalSchemaGenerator;
import org.openforis.collect.relational.model.Table;
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
	protected void execute() throws Throwable {
		dropInputSchema();
		
		RelationalSchema schema = createInputSchema();
		
		grantSelectPermissionsToCalcUser(schema);
	}

	private void grantSelectPermissionsToCalcUser(RelationalSchema rdbSchema) {
		//grant schema usage
		SchemaImpl inputSchema = new SchemaImpl(rdbSchema.getName());
		psql()
			.grant(Privilege.USAGE)
			.onSchema(inputSchema)
			.to(getCalcUser())
			.execute();
		//grant select on all tables
		for (Table<?> rdbTable : rdbSchema.getTables()) {
			AbstractTable table = new AbstractTable(rdbTable.getName(), inputSchema) {
				private static final long serialVersionUID = 1L;
			};
			psql()
				.grant(Privilege.SELECT)
				.on(table)
				.to(getCalcUser())
				.execute();
		}
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
	
}
