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
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.liquibase.LiquibaseRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

/**
 * @author S. Ricci
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CollectInputSchemaCreatorTask extends Task {

	@Autowired
	private Configuration config;
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Override
	public String getName() {
		return "Create database";
	}
	
	@Override
	protected long countTotalItems() {
		return 3;
	}
	
	@Override
	protected void execute() throws Throwable {
		dropInputSchema();
		
		createInputSchema();
		
		addUserDefinedVariableColumns();
		
//		addVariablePerHaColumns();
		
//		createViews();
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
		RelationalSchema schema = ((CollectBackupImportJob) getJob()).getInputRelationalSchema();
		RelationalSchemaCreator relationalSchemaCreator = new LiquibaseRelationalSchemaCreator();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		relationalSchemaCreator.createRelationalSchema(schema, connection);
		incrementItemsProcessed();
		return schema;
	}
	
	private void addUserDefinedVariableColumns() {
		workspaceService.addUserDefinedVariableColumns(getWorkspace());
		incrementItemsProcessed();
	}

//	private void addVariablePerHaColumns() {
//		workspaceService.addVariablePerHaColumns(getWorkspace());
//		incrementItemsProcessed();
//	}

//	private void createViews() {
//		workspaceService.createViews(getWorkspace());
//		incrementItemsProcessed();
//	}

}
