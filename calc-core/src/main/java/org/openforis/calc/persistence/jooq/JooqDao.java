package org.openforis.calc.persistence.jooq;

import java.util.Collection;
import java.util.List;

import org.jooq.DAO;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.jooq.UpdatableRecord;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DAOImpl;
import org.jooq.impl.Factory;
import org.openforis.calc.common.Identifiable;

/**
 * Decorator around jOOQ's default DAO implementation adding additional
 * functionality and behavior. In particular, in addition to
 * {@link UpdatableRecord}, this DAO may be used read operations with
 * {@link TableRecord}. Note that the behavior of calling update operations with
 * non-updatable records is undefined.
 * 
 * @author G. Miceli
 * 
 * @param <R>
 * @param <P>
 * @param <T>
 */
public class JooqDao<R extends TableRecord<R>, P, T> implements DAO<R, P, T> {
	private DAO<R, P, T> dao;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	JooqDao(Table<R> table, Class<P> type, Factory jooqFactory) {
		this.dao = new DAOImpl(table, type, jooqFactory) {
			@Override
			protected Object getId(Object object) {
				return getId(object);
			}
		};
	}

	public Integer getId(P object) {
		if (object == null) {
			throw new NullPointerException();
		} else if (object instanceof Identifiable) {
			return ((Identifiable) object).getId();
		} else {
			throw new IllegalArgumentException(object.getClass().toString());
		}
	}

	public void insert(P object) throws DataAccessException {
		dao.insert(object);
	}

	public void insert(P... objects) throws DataAccessException {
		dao.insert(objects);
	}

	public void insert(Collection<P> objects) throws DataAccessException {
		dao.insert(objects);
	}

	public void update(P object) throws DataAccessException {
		dao.update(object);
	}

	public void update(P... objects) throws DataAccessException {
		dao.update(objects);
	}

	public void update(Collection<P> objects) throws DataAccessException {
		dao.update(objects);
	}

	public void delete(P... objects) throws DataAccessException {
		dao.delete(objects);
	}

	public void delete(Collection<P> objects) throws DataAccessException {
		dao.delete(objects);
	}

	public void deleteById(T... ids) throws DataAccessException {
		dao.deleteById(ids);
	}

	public void deleteById(Collection<T> ids) throws DataAccessException {
		dao.deleteById(ids);
	}

	public boolean exists(P object) throws DataAccessException {
		return dao.exists(object);
	}

	public boolean existsById(T id) throws DataAccessException {
		return dao.existsById(id);
	}

	public long count() throws DataAccessException {
		return dao.count();
	}

	public List<P> findAll() throws DataAccessException {
		return dao.findAll();
	}

	public P findById(T id) throws DataAccessException {
		return dao.findById(id);
	}

	public <Z> List<P> fetch(Field<Z> field, Z... values)
			throws DataAccessException {
		return dao.fetch(field, values);
	}

	public <Z> P fetchOne(Field<Z> field, Z value) throws DataAccessException {
		return dao.fetchOne(field, value);
	}

	public Table<R> getTable() {
		return dao.getTable();
	}

	public Class<P> getType() {
		return dao.getType();
	}

}