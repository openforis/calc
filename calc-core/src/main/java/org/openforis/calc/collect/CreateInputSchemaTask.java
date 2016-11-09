/**
 * 
 */
package org.openforis.calc.collect;

import java.sql.Connection;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DynamicTable;
import org.jooq.impl.SchemaImpl;
import org.openforis.calc.engine.Task;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.ExtendedSchema;
import org.openforis.collect.relational.CollectRdbException;
import org.openforis.collect.relational.RelationalSchemaCreator;
import org.openforis.collect.relational.jooq.JooqRelationalSchemaCreator;
import org.openforis.collect.relational.model.RelationalSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * @author S. Ricci
 * @author M. Togna
 *
 */
public class CreateInputSchemaTask extends Task {

	@Autowired
	private Configuration config;

	@Autowired
	private Psql psql;

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

		createExtendedSchema();
	}

	private void dropInputSchema() {
		String inputSchemaName = getWorkspace().getInputSchema();
		psql().dropSchemaIfExists(new SchemaImpl(inputSchemaName)).cascade().execute();

		incrementItemsProcessed();
	}

	private RelationalSchema createInputSchema() throws CollectRdbException {
		String inputSchemaName = getWorkspace().getInputSchema();
		psql().createSchema(new SchemaImpl(inputSchemaName)).execute();
		DataSourceConnectionProvider connectionProvider = (DataSourceConnectionProvider) config.connectionProvider();
		DataSource dataSource = connectionProvider.dataSource();
		RelationalSchema schema = ((CollectSurveyImportJob) getJob()).getInputRelationalSchema();
		RelationalSchemaCreator relationalSchemaCreator = new JooqRelationalSchemaCreator();
		Connection connection = DataSourceUtils.getConnection(dataSource);
		relationalSchemaCreator.createRelationalSchema(schema, connection);

		createBooleanCodeTable(inputSchemaName);

		incrementItemsProcessed();

		return schema;
	}

	private void createBooleanCodeTable(String schema) {
		BooleanCodeListTable table = new BooleanCodeListTable(schema);
		psql().dropTableIfExistsLegacy(table);
		psql().createTable(table , table.fields() ).execute();
		
		psql()
			.insertInto(table , table.getIdField() , table.getCodeField() , table.getLabelField() )
			.values( "null" , "-1" , "N/A" )
			.values( "true" , "true" , "True" )
			.values( "false" , "false" , "False" )
			.execute();
	}

	private void createExtendedSchema() {
		
		String extendedSchema = ExtendedSchema.getName( getWorkspace() );
		
		DynamicTable<Record> schemata = new DynamicTable<Record>("schemata" ,"information_schema");
		Field<String> schemaName = schemata.getVarcharField("schema_name");

		Integer count = psql
					.selectCount()
					.from( schemata )
					.where( schemaName.eq(extendedSchema) )
					.fetchOne( DSL.count() );
		
		if( count == 0 ) {
			psql
				.createSchema( new SchemaImpl(extendedSchema) )
				.execute();
		}
		
		incrementItemsProcessed();
	}
}
