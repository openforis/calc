package org.openforis.calc.psql;

import java.math.BigDecimal;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Schema;
import org.jooq.Select;
import org.jooq.Table;
import org.jooq.Update;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple PostreSQL query builder
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public final class Psql extends DefaultDSLContext {
	private static final long serialVersionUID = 1L;

	public static final DataType<BigDecimal> DOUBLE_PRECISION = SQLDataType.NUMERIC.precision(15, 5);
	public static final DataType<Long> SERIAL = new SerialDataType();
	public static final DataType<GeodeticCoordinate> GEODETIC_COORDINATE = new GeodeticCoordinateDataType();

	public static final Schema PUBLIC = DSL.schemaByName("public");

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger( Psql.class );

	public enum Privilege {

		USAGE, ALL, SELECT;

		public String toString() {
			return name().toLowerCase();
		};
	};

	public Psql(Configuration configuration) {
		super(configuration);
	}

	//	@Deprecated
	public Psql() {
		super(SQLDialect.POSTGRES);
		init();
	}

//	@Deprecated
	public Psql(DataSource dataSource) {
		super(dataSource, SQLDialect.POSTGRES);
		init();
	}

	public Psql(SQLDialect dialect) {
		super( dialect);
	}

	private void init() {
		configuration().set(new DefaultExecuteListenerProvider(new LogSqlListener()));
	}

	static String[] names(Field<?>[] fields) {
		String[] names = new String[fields.length];
		for (int i = 0; i < fields.length; i++) {
			names[i] = fields[i].getName();
		}
		return names;
	}

	public GrantStep grant(Privilege... privileges) {
		return new GrantStep(this, privileges);
	}

	public CreateTableStep createTable(Table<?> table) {
		return new CreateTableStep(this, table);
	}

	public CreateViewStep createView(Table<?> table) {
		return new CreateViewStep(this, table);
	}

	public Select<?> selectStarFrom(Table<?> table) {
		String tableName = table.getName();
		String schemaName = table.getSchema().getName();
		return select().from(DSL.tableByName(schemaName, tableName));
	}

	public AlterTableStep alterTable(Table<?> table) {
		return new AlterTableStep(this, table);
	}

	public CreateSchemaStep createSchema(Schema schema) {
		return new CreateSchemaStep(this, schema);
	}

	public DropSchemaStep dropSchemaIfExists(Schema schema) {
		return new DropSchemaStep(this, true, schema);
	}

	public SetDefaultSchemaSearchPathStep setDefaultSchemaSearchPath(Schema... schemas) {
		return new SetDefaultSchemaSearchPathStep(this, schemas);
	}

	public UpdateWithStep updateWith(Table<?> cursor, Update<?> update, Object joinCondition) {
		return new UpdateWithStep(this, cursor, update, joinCondition);
	}

	public DropTableStep dropTableIfExists(Table<?> table) {
		return new DropTableStep(this, true, table);
	}

	public DropViewStep dropViewIfExists(Table<?> table) {
		return new DropViewStep(this, true, table);
	}

	public CaseStep decode() {
		return new CaseStep(this);
	}

	public CreateTableWithFieldsStep createTable(Table<?> table, Field<?>... fields) {
		return new CreateTableWithFieldsStep(this, table, fields);
	}

	public static <T> Field<T> typedNull(DataType<T> type) {
		return DSL.val(null).cast(type);
	}

	public static <T> Field<T> nullAs(Field<T> field) {
		return typedNull(field.getDataType()).as(field.getName());
	}

	public static Field<?> nullValue(Field<Integer> stratumId) {
		// TODO Auto-generated method stub
		return null;
	}
}
