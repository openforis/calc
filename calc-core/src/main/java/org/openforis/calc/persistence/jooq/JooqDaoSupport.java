package org.openforis.calc.persistence.jooq;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jooq.DAO;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.Table;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;
import org.jooq.UpdatableTable;
import org.jooq.impl.DAOImpl;
import org.jooq.impl.Factory;
import org.openforis.calc.io.FlatDataStream;
import org.openforis.calc.model.Identifiable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 */
public abstract class JooqDaoSupport<R extends UpdatableRecord<R>, P extends Identifiable>
	extends JdbcDaoSupport implements DAO<R, P, Integer>
{
	private Log log = LogFactory.getLog(getClass());

	private Table<R> table;
	private Class<P> type;

	@Autowired(required = true)
	@Qualifier("dataSource")
	private void setDataSourceInternal(DataSource dataSource) {
		setDataSource(dataSource);
	}
	
	protected JooqDaoSupport(Table<R> table, Class<P> type) {
		this.table = table;
		this.type = type;
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
    	
        List<R> records = records(pojos, false);
        
        // Execute a batch INSERT
		if (objects.size() > 1) {
            create.batchStore(records).execute();
        }
        // Execute a regular INSERT
        else if (objects.size() == 1) {
            records.get(0).store();
        }
        
		// Update ids in POJOs
		Field<?> pk = pk();
		if ( !objects.isEmpty() && pk != null ) {
			Iterator<R> recordIter = records.iterator();
			for (P pojo : pojos) {
				if ( pojo instanceof Identifiable ) {
					R record = recordIter.next();
					Integer id = (Integer) record.getValue(pk());
					pojo.setId(id);
				}
			}
		}
    }
	
//	
//	@Override
//	@Transactional
//	public void insert(P object) {
//		JooqDaoImpl jooqDao = getJooqDao();
//		jooqDao.insert(object);
//	}
//
//	@Override
//	@Transactional
//	public void insert(P... objects) {
//		JooqDaoImpl jooqDao = getJooqDao();
//		jooqDao.insert(objects);
//	}
//
//	@Override
//	@Transactional
//	public void insert(Collection<P> objects) {
//		JooqDaoImpl jooqDao = getJooqDao();
//		jooqDao.insert(objects);
//	}

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
		return jooqDao.findById(id);
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
		return jooqDao.fetchOne(field, value);
	}

	protected static Timestamp toTimestamp(Date date) {
		if ( date == null ) {
			return null;
		} else {
			return new Timestamp(date.getTime());
		}
	}
	
	public class JooqDaoImpl extends DAOImpl<R, P, Integer> {

		protected JooqDaoImpl(Table<R> table, Class<P> type, Factory create) {
			super(table, type, create);
		}

		@Override
		protected Integer getId(P object) {
			return object.getId();
		}
	}
	
	// From DAOImpl
	
    private Field<?> pk() {
        if (table instanceof UpdatableTable) {
            UpdatableTable<?> updatable = (UpdatableTable<?>) table;
            UniqueKey<?> key = updatable.getMainKey();

            if (key.getFields().size() == 1) {
                return key.getFields().get(0);
            }
        }

        return null;
    }

    private List<R> records(Collection<P> objects, boolean forUpdate) {
    	Factory create = getJooqFactory();
        List<R> result = new ArrayList<R>();
//        Field<?> pk = pk();

        for (P object : objects) {
            R record = create.newRecord(table, object);

//            if (forUpdate && pk != null) {
//                ((AbstractRecord) record).getValue0(pk).setChanged(false);
//            }

            result.add(record);
        }

        return result;
    }

    protected FlatDataStream toDataStream(Result<? extends Record> result) {
		return new JooqResultDataStream(result);
	}
}
