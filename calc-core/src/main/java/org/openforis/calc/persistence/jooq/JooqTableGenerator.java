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
import org.openforis.calc.persistence.jooq.tables.OlapTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * 
 */
@Component
public class JooqTableGenerator {

	private static final int maxTableNameLength = 256;

	@Autowired
	private DataSource dataSource;

	private Platform platform;

	public JooqTableGenerator() {
	}

	@PostConstruct
	protected void init() {
		platform = PlatformFactory.createNewPlatformInstance(dataSource);
		PlatformInfo platformInfo = platform.getPlatformInfo();
		platformInfo.addNativeTypeMapping(GeodeticCoordinate.SQL_TYPE_CODE, GeodeticCoordinate.SQL_TYPE_NAME);
		platformInfo.setMaxTableNameLength(maxTableNameLength);
	}

	// @Transactional(propagation = Propagation.REQUIRES_NEW)
	// synchronized
	// public void drop(UpdatableTableImpl<?> jooqTable) {
	// Database database = initDatabase();
	// Table table = initTable(database, jooqTable);
	//
	// dropTable(database, table);
	// }

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	synchronized public void create(UpdatableTableImpl<?> jooqTable) {
		Database database = initDatabase();
		Table table = initTable(database, jooqTable);
		initColumns(table, jooqTable);

		// dropTable(database, table);
		createTable(database);
	}

	// @Transactional
	// synchronized
	// public void generate(UpdatableTableImpl<?> jooqTable) {
	// Database database = initDatabase();
	// Table table = initTable(database, jooqTable);
	// initColumns(table, jooqTable);
	//
	// dropTable(database, table);
	// createTable(database);
	// }

	// private void dropTable(Database database, Table table) {
	//
	// try {
	// platform.dropTable(database, table, false);
	// } catch ( DatabaseOperationException e ) {
	// e.printStackTrace();
	// }
	// }

	private void createTable(Database database) {
		platform.createTables(database, false, false);
	}

	private void initColumns(Table table, UpdatableTableImpl<?> jooqTable) {

		List<Field<?>> fields = jooqTable.getFields();
		for ( Field<?> field : fields ) {
			DataType<?> dataType = field.getDataType();
			int sqlType = dataType.getSQLType();

			boolean primarykey = isPrimarykey(field, jooqTable);

			addColumn(table, field.getName(), sqlType, primarykey);
		}
	}

	private boolean isPrimarykey(Field<?> field, UpdatableTableImpl<?> jooqTable) {
		if ( jooqTable instanceof OlapTable ) {
			return ((OlapTable<?>) jooqTable).ID.equals(field);
		} else {
			UniqueKey<?> mainKey = jooqTable.getMainKey();
			return mainKey.getFields().contains(field);
		}
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
