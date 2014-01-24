/**
 * 
 */
package org.openforis.calc.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.impl.CsvFileTable;
import org.json.simple.JSONArray;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Manager for importing csv file as table
 * 
 * @author Mino Togna
 * 
 */
// @Component
public class CsvDataImportTask extends Task {

	private CsvReader csvReader;
	private CsvFileTable table;

	public CsvDataImportTask(String filepath, String table, JSONArray colOptions) throws IOException {
		this.table = new CsvFileTable(table, colOptions);

		this.csvReader = new CsvReader(filepath);
		this.csvReader.readHeaders();
		
	}

	@Override
	protected long countTotalItems() {
		try {
			return csvReader.size() - 1;
		} catch (IOException e) {
			throw new RuntimeException("Error while reading csvReader size", e);
		}
	}

	@Override
	protected void execute() throws Throwable {

		// drop table if it exists
		new Psql(getDataSource()).dropTableIfExists(table).execute();
		// create table
		new Psql(getDataSource()).createTable(table, table.fields()).execute();
		// add pkey to table
		new Psql(getDataSource()).alterTable(table).addPrimaryKey(table.getPrimaryKey()).execute();

		populateTable(table);
	}

	// iterate over records to import and creates insert queries
	@Transactional
	private <T extends Object> void populateTable(CsvFileTable table) throws IOException {

		List<InsertQuery<Record>> queries = new ArrayList<InsertQuery<Record>>();
		for (FlatRecord record = this.csvReader.nextRecord(); record != null; record = this.csvReader.nextRecord()) {

			InsertQuery<Record> insert = new Psql(this.getDataSource()).insertQuery(table);
			@SuppressWarnings("unchecked")
			Field<T>[] fields = (Field<T>[]) table.fields();
			for (Field<T> field : fields) {
				String name = field.getName();
				if (table.ID != field) {
					Class<T> type = field.getDataType().getType();
					T value = record.getValue(name, type);
					insert.addValue(field, value);
				}
			}

			queries.add(insert);
			if (queries.size() % 5000 == 0) {
				executeBatch(queries);
			}

			incrementItemsProcessed();
		}

		if (!queries.isEmpty()) {
			executeBatch(queries);
		}
	}

	private void executeBatch(List<InsertQuery<Record>> queries) {
		new Psql(this.getDataSource()).batch(queries).execute();
		queries.clear();
	}

	@Override
	public String getName() {
		return "Import csv into database table";
	}
	
	@JsonInclude
	public String getSchema(){
		return table.getSchema().getName();
	}
	
	@JsonInclude
	public String getTable(){
		return table.getName();
	}

}
