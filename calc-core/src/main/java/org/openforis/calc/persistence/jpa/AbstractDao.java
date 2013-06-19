package org.openforis.calc.persistence.jpa;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.common.ReflectionUtils;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * @param <T>
 */
@Repository
public abstract class AbstractDao<T extends Identifiable> {
	
    @PersistenceContext
    private EntityManager entityManager;

    private Class<T> type;
    
    protected AbstractDao() {
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
			object = create(object);
		} else {
			object = update(object);
		}
		return object;
	}
	
	public void delete(int id) {
		T ref = entityManager.getReference(type, id);
		entityManager.remove(ref);
	}
	
	public List<T> loadAll() {
		TypedQuery<T> q = entityManager.createQuery("select a from "+type.getSimpleName()+" a", type);
		q.setHint("org.hibernate.cacheable", true);

		List<T> list = q.getResultList();
		return list;
	}
	
	@PostConstruct
	void initType(){
		this.type = ReflectionUtils.extractGenericType(getClass());
	}
}
