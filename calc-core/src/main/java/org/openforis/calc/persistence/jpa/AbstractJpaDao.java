package org.openforis.calc.persistence.jpa;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.persistence.Dao;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
public abstract class AbstractJpaDao<T extends Identifiable> implements Dao<T> {
    @PersistenceContext
    private EntityManager entityManager;

    private Class<T> type;
    
    protected AbstractJpaDao() {
       this.type = extractGenericType(); 
	}
    
	@Override
	public T create(T object) {
		entityManager.persist(object);
		return object;
	}

	@Override
	public T find(int id) {
		return (T) entityManager.find(type, id);
	}

	@Override
	public T update(T object) {
		return entityManager.merge(object);
	}

	@Override
	public void delete(int id) {
		T ref = entityManager.getReference(type, id);
		entityManager.remove(ref);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Class<T> extractGenericType() {
	   Type t = getClass().getGenericSuperclass();
       ParameterizedType pt = (ParameterizedType) t;
       return (Class) pt.getActualTypeArguments()[0];
	}
}
