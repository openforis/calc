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

	private CollectSurvey survey;
	
	@Autowired
	private Configuration config;
	
	@Override
	protected void execute() throws Throwable {
		dropInputSchema();
		
		createInputSchema();
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
		RelationalSchema schema = rdbGenerator.generateSchema(survey, inputSchemaName);
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		relationalSchemaCreator.createRelationalSchema(schema, connection);
		return schema;
	}
	
	public CollectSurvey getSurvey() {
		return survey;
	}

	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}

}
