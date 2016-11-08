package org.openforis.calc.psql;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
import org.jooq.util.postgres.PostgresDataType;
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

	public static final Map<String,DataType<?>> POSTGRESQL_DATA_TYPES ;
	
	public enum Privilege {

		USAGE, ALL, SELECT;

		public String toString() {
			return name().toLowerCase();
		};
	};
	
	static {
		// init postgresql data types
		try {
			POSTGRESQL_DATA_TYPES = new HashMap<String, DataType<?>>();
			
			java.lang.reflect.Field[] fields = PostgresDataType.class.getDeclaredFields();
			for ( java.lang.reflect.Field field : fields ) {
				field.setAccessible(true);
				if ( java.lang.reflect.Modifier.isStatic( field.getModifiers() ) ) {
			        Object object = field.get( null );
			        if( object instanceof DataType<?> ){
			        	DataType<?> dataType = (DataType<?>) object;
			        	POSTGRESQL_DATA_TYPES.put( dataType.getTypeName(), dataType );
			        }
			    }
			}
		} catch ( Exception e ) {
			throw new RuntimeException( "Error while loading postgreSQL datatypes " , e );
		}
	}

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

	public CreateTableStep createTableLegacy(Table<?> table) {
		return new CreateTableStep(this, table);
	}

	public CreateViewStep createViewLegacy(Table<?> table) {
		return new CreateViewStep(this, table);
	}

	public Select<?> selectStarFrom(Table<?> table) {
		String tableName = table.getName();
		String schemaName = table.getSchema().getName();
		return select().from(DSL.tableByName(schemaName, tableName));
	}

	public AlterTableStep alterTableLegacy(Table<?> table) {
		return new AlterTableStep(this, table);
	}

	public CreateSchemaStep createSchema(Schema schema) {
		return new CreateSchemaStep(this, schema);
	}

	public DropSchemaStep dropSchemaIfExists(Schema schema) {
		return new DropSchemaStep(this, true, schema);
	}

	public DropSchemaStep dropSchemaIfExistsCascade(Schema schema) {
		return new DropSchemaStep(this, true, schema, true );
	}
	
	public SetDefaultSchemaSearchPathStep setDefaultSchemaSearchPath(Schema... schemas) {
		return new SetDefaultSchemaSearchPathStep(this, schemas);
	}

	public UpdateWithStep updateWith(Table<?> cursor, Update<?> update, Object joinCondition) {
		return new UpdateWithStep(this, cursor, update, joinCondition);
	}

	public DropTableStep dropTableIfExistsLegacy(Table<?> table) {
		return new DropTableStep(this, true, table);
	}

	public DropViewStep dropViewIfExistsLegacy(Table<?> table) {
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
