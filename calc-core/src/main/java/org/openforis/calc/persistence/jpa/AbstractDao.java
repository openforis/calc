package org.openforis.calc.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.common.ReflectionUtils;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
public abstract class AbstractDao<T extends Identifiable> {
    @PersistenceContext
    private EntityManager entityManager;

    private Class<T> type;
    
    protected AbstractDao() {
       this.type = ReflectionUtils.extractGenericType(getClass());
	}
    
	public T create(T object) {
		entityManager.persist(object);
		return object;
	}

	public T find(int id) {
		return (T) entityManager.find(type, id);
	}

	public T update(T object) {
		return entityManager.merge(object);
	}

	public T save(T object) {
		if ( object.getId() == null ) {
			return create(object);
		} else {
			return update(object);
		}
	}
	
	public void delete(int id) {
		T ref = entityManager.getReference(type, id);
		entityManager.remove(ref);
	}
}
