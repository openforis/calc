/**
 * 
 */
package org.openforis.calc.persistence.jooq;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.model.Column;
import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;
import org.jooq.impl.UpdatableTableImpl;
import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author G. Miceli
 */
@Component
// TODO Why singleton/component instead of normal Java class? 
public class DdlGenerator {

	private static final int MAX_TABLE_NAME_LENGTH = 255;

	private static final int MAX_IDENTIFIER_LENGTH = 255;

	@Autowired
	private DataSource dataSource;
	
	
	private Platform platform;

	public DdlGenerator() {
	}

	@PostConstruct
	protected void init() {
		platform = PlatformFactory.createNewPlatformInstance(dataSource);
		PlatformInfo platformInfo = platform.getPlatformInfo();
		platformInfo.addNativeTypeMapping(GeodeticCoordinate.SQL_TYPE_CODE, GeodeticCoordinate.SQL_TYPE_NAME);
		platformInfo.setMaxTableNameLength(MAX_TABLE_NAME_LENGTH);
		platformInfo.setMaxIdentifierLength(MAX_IDENTIFIER_LENGTH);
	}

	@Transactional
	synchronized 
	public void createTable(UpdatableTableImpl<?> jooqTable) {
		Database database = initDatabase();
		Table table = initTable(database, jooqTable);
		initColumns(table, jooqTable);

		// dropTable(database, table);
		createTable(database);
	}

	private void createTable(Database database) {
		platform.createTables(database, false, false);
	}

	private void initColumns(Table table, UpdatableTableImpl<?> jooqTable) {

		List<Field<?>> fields = jooqTable.getFields();
		for ( Field<?> field : fields ) {
			DataType<?> dataType = field.getDataType();
			int sqlType = dataType.getSQLType();

			boolean primarykey = isPrimaryKey(field, jooqTable);

			addColumn(table, field.getName(), sqlType, primarykey);
		}
	}

	private boolean isPrimaryKey(Field<?> field, UpdatableTableImpl<?> jooqTable) {
		UniqueKey<?> mainKey = jooqTable.getMainKey();
		return mainKey.getFields().contains(field);
	}

	private Table initTable(Database database, TableImpl<?> jooqTable) {
		Table table = new Table();

		String tableName = jooqTable.getSchema().getName() + "." + jooqTable.getName();
		table.setName(tableName);

		database.addTable(table);
		return table;
	}

	private Database initDatabase() {
		Database database = new Database();
		return database;
	}

	private Column addColumn(Table table, String name, int typeCode, boolean primaryKey) {
		Column col = new Column();
		col.setName(name);
		col.setTypeCode(typeCode);
		if ( primaryKey ) {
			col.setAutoIncrement(true);
			col.setPrimaryKey(true);
		}
		table.addColumn(col);
		return col;
	}

}
