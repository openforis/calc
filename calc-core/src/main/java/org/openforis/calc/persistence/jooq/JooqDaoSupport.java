package org.openforis.calc.persistence.jooq;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.DAO;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectSelectStep;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;
import org.jooq.UpdatableTable;
import org.jooq.impl.DAOImpl;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.Identifiable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public abstract class JooqDaoSupport<R extends TableRecord<R>, P>
	extends JdbcDaoSupport implements DAO<R, P, Integer>
{
	private Log log = LogFactory.getLog(getClass());

	private Table<R> table;
	private Class<P> type;
	private Field<?>[] uniqueKeyFields;
	private Map<List<Object>, Integer> idCache;
	// TODO sync cache on insert, update, delete - in jooq factory?
	private Field<?>[] requiredFields;

	
	protected JooqDaoSupport(Table<R> table, Class<P> type, Field<?>... uniqueKeyFields) {
		this.table = table;
		this.type = type;
		this.uniqueKeyFields = uniqueKeyFields;
	}

	protected void require(Field<?>... requiredFields ) {
		this.requiredFields = requiredFields;
	}

	@PostConstruct
	private void preloadKeys() {
		if ( uniqueKeyFields!= null && uniqueKeyFields.length > 0 ) {
			log.info("Pre-loading keys from "+table);
			this.idCache = new HashMap<List<Object>, Integer>();
			Result<Record> result = fetchKeys();
			for (Record record : result) {
				putId(record);
			}
		}
	}

	private Result<Record> fetchKeys() {
		Factory create = getJooqFactory();
		List<Field<?>> fields = getKeyFields();
		Result<Record> result = create.select(fields)
									   .from(table)
									   .fetch();
		return result;
	}

	private List<Field<?>> getKeyFields() {
		List<Field<?>> fields = new ArrayList<Field<?>>(uniqueKeyFields.length+1);
		fields.add(pk());
		fields.addAll(Arrays.asList(uniqueKeyFields));
		return fields;
	}

	protected void putId(Record record) {
		if ( idCache != null ) {
			List<Object> keys = getKey(record);
			Integer id = record.getValueAsInteger(pk());
			idCache.put(keys, id);
		}
	}

	protected List<Object> getKey(Record record) {
		List<Object> keys = new ArrayList<Object>(uniqueKeyFields.length);
		for (Field<?> uk : uniqueKeyFields) {
			Object val = record.getValue(uk);
			keys.add(val);
		}
		return keys;
	}
	
	@Autowired(required = true)
	@Qualifier("dataSource")
	private void setDataSourceInternal(DataSource dataSource) {
		setDataSource(dataSource);
	}
	
	public Integer getIdByKey(Object... keys) {
		if ( keys == null ) {
			throw new NullPointerException("keys");
		}
		if ( uniqueKeyFields == null ) {
			throw new NullPointerException("uniqueKeyFields");
		}
		if ( keys.length != uniqueKeyFields.length ) {
			throw new IllegalArgumentException("Expected "+uniqueKeyFields.length+" but got "+keys.length);
		}
		return idCache.get(Arrays.asList(keys));
	}

	protected Log getLog() {
		return log;
	}
	
	protected DialectAwareJooqFactory getJooqFactory() {
		Connection connection = getConnection();
		return new DialectAwareJooqFactory(connection);
	}

	protected JooqDaoImpl getJooqDao() {
		Factory factory = getJooqFactory();
		return new JooqDaoImpl(table, type, factory);
	}
	
	@Override
	public Table<R> getTable() {
		return table;
	}
	
	@Override
	public Class<P> getType() {
		return type;
	}
	

	@Transactional
    @Override
    public void insert(P object) {
        insert(singletonList(object));
    }

    @Transactional
    @Override
    public void insert(P... objects) {
        insert(asList(objects));
    }

    @Transactional
    @Override
    public void insert(Collection<P> objects) {
    	Factory create = getJooqFactory();
    	List<P> pojos = new ArrayList<P>(objects);
    	
        List<UpdatableRecord<?>> records = records(pojos, false);
        
        // Execute a batch INSERT
		if (objects.size() > 1) {
            create.batchStore(records).execute();
        }
        // Execute a regular INSERT
        else if (objects.size() == 1) {
            records.get(0).store();
        }
        
		// Update ids in POJOs and cache
		Field<?> pk = pk();
		if ( !objects.isEmpty() && pk != null ) {
			Iterator<UpdatableRecord<?>> recordIter = records.iterator();
			for (P pojo : pojos) {
				UpdatableRecord<?> record = recordIter.next();
				if ( pojo instanceof Identifiable ) {
					Integer id = (Integer) record.getValue(pk());
					((Identifiable) pojo).setId(id);
				}
				putId(record);
			}
		}
    }

	@Override
	@Transactional
	public void update(P object) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.update(object);
	}

	@Override
	@Transactional
	public void update(P... objects) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.update(objects);
	}

	@Override
	@Transactional
	public void update(Collection<P> objects) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.update(objects);
	}

	@Override
	@Transactional
	public void delete(P... objects) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.delete(objects);
	}

	@Override
	@Transactional
	public void delete(Collection<P> objects) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.delete(objects);
	}

	@Override
	@Transactional
	public void deleteById(Integer... ids) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.deleteById(ids);
	}

	@Override
	@Transactional
	public void deleteById(Collection<Integer> ids) {
		JooqDaoImpl jooqDao = getJooqDao();
		jooqDao.deleteById(ids);
	}

	@Override
	@Transactional
	public boolean exists(P object) {
		JooqDaoImpl jooqDao = getJooqDao();
		return jooqDao.exists(object);
	}

	@Override
	@Transactional
	public boolean existsById(Integer id) {
		JooqDaoImpl jooqDao = getJooqDao();
		return jooqDao.existsById(id);
	}

	@Override
	@Transactional
	public long count() {
		JooqDaoImpl jooqDao = getJooqDao();
		return jooqDao.count();
	}

	@Override
	@Transactional
	public List<P> findAll() {
		JooqDaoImpl jooqDao = getJooqDao();
		return jooqDao.findAll();
	}

	@Override
	@Transactional
	public P findById(Integer id) {
		JooqDaoImpl jooqDao = getJooqDao();
		return (P) jooqDao.findById(id);
	}

	@Override
	@Transactional
	public <Z> List<P> fetch(Field<Z> field, Z... values) {
		JooqDaoImpl jooqDao = getJooqDao();
		return jooqDao.fetch(field, values);
	}

	@Override
	@Transactional
	public <Z> P fetchOne(Field<Z> field, Z value) {
		JooqDaoImpl jooqDao = getJooqDao();
		return (P) jooqDao.fetchOne(field, value);
	}


	@Transactional
	public <Z> FlatDataStream stream(Field<?>[] fields, Field<Z> filterField, Z... values) {
		Factory create = getJooqFactory();
        return stream(
        		create.select(fields)
        			.from(table)
        			.where(filterField.in(values))
        			.fetch());
	}

	@Transactional
	public <Z> FlatDataStream stream(String[] fieldNames, Field<Z> filterField, Z... values) {
		Field<?>[] fields = getFields(fieldNames);
		return stream(fields, filterField, values);
	}
//	@Transactional
//	public <Z> FlatDataStream streamOne(Field<?>[] fields, Field<Z> filterField, Z value) {
//		Factory create = getJooqFactory();
//        return stream(
//        		create.select(fields)
//        			.from(table)
//        			.where(filterField.equal(value))
//        			.fetchOne());
//	}

	protected Field<?>[] getFields(String[] fieldNames) {
		if ( fieldNames == null ) {
			return new Field<?>[0];
		} else {
			Field<?>[] fields = new Field<?>[fieldNames.length];
			for (int i = 0; i < fieldNames.length; i++) {
				String name = fieldNames[i];
				Field<?> field = table.getField(name);
				if ( field == null ) {
					throw InvalidFieldNameException.forFieldName(name);
				}
				fields[i] = field;
			}
			return fields;
		}
	}

	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}
	
	private class JooqDaoImpl<UR extends UpdatableRecord<UR>> extends DAOImpl<UR, P, Integer> {

		private JooqDaoImpl(Table<UR> table, Class<P> type, Factory create) {
			super(table, type, create);
		}

		@Override
		protected Integer getId(P object) {			
			return ((Identifiable) object).getId();
		}
	}
	
	// From DAOImpl
	
    protected Field<?> pk() {
        if (table instanceof UpdatableTable) {
            UpdatableTable<?> updatable = (UpdatableTable<?>) table;
            UniqueKey<?> key = updatable.getMainKey();

            if (key.getFields().size() == 1) {
                return key.getFields().get(0);
            }
        }

        return null;
    }

    private List<UpdatableRecord<?>> records(Collection<P> objects, boolean forUpdate) {
    	Factory create = getJooqFactory();
        List<UpdatableRecord<?>> result = new ArrayList<UpdatableRecord<?>>();
//        Field<?> pk = pk();

        for (P object : objects) {
        	UpdatableRecord<?> record = (UpdatableRecord<?>) create.newRecord(table, object);

//            if (forUpdate && pk != null) {
//                ((AbstractRecord) record).getValue0(pk).setChanged(false);
//            }

            result.add(record);
        }

        return result;
    }

    protected FlatDataStream stream(Result<? extends Record> result) {
		return new JooqResultDataStream(result);
	}
    
	protected FlatDataStream stream(R record) {
		return new JooqResultDataStream(record);
	}
	
	protected Field<?>[] getUniqueKeyFields() {
		return uniqueKeyFields;
	}
	
	protected Object[] extractKey(FlatRecord r, Object... fieldsOrValues) {
		Object[] keys = new Object[fieldsOrValues.length];
		if ( uniqueKeyFields.length != fieldsOrValues.length ) {
			throw new IllegalArgumentException("Wrong number of keys");
		}
		for (int i = 0; i < fieldsOrValues.length; i++) {
			Object fov = fieldsOrValues[i];
			if ( fov instanceof Field ) {
				Field<?> field = (Field<?>) fov;
				String name = field.getName();
				keys[i] = r.getValue(name, field.getType());
			} else {
				keys[i] = fov;
			}
		}
		return keys;
	}
	
	// TODO refactor?

	protected void copyFields(FlatRecord from, Record to, Field<?>... fields) {
		for (Field<?> field : fields) {
			copyField(from, to, field);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void copyField(FlatRecord from, Record to, Field field) {		
		String name = field.getName();
		Class<?> type = field.getType();
		Object value = from.getValue(name, type);
		to.setValue(field, value);
	}
	
	protected R toJooqRecord(FlatRecord r) {
		R record = getJooqFactory().newRecord(table);
		List<Field<?>> fields = table.getFields();
		copyFields(r, record, fields.toArray(new Field<?>[fields.size()]));
		return record;
	}
	
	public boolean isValid(FlatRecord r) {
		if ( requiredFields != null ) {
			for (Field<?> field : requiredFields) {
				if ( r.isMissing(field.getName()) ) {
					return false;
				}
			}
		}
		return true;
	}

	protected SelectSelectStep selectByName(String[] fieldNames) {
		Factory create = getJooqFactory();
		Field<?>[] fields = getFields(fieldNames);
		return create.select(fields);		
	}
}
