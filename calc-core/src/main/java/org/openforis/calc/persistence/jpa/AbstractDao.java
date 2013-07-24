package org.openforis.calc.persistence.jpa;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.common.ReflectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Transactional
	public T create(T object) {
		entityManager.persist(object);
		return object;
	}

    @Transactional
	public T find(int id) {
		return (T) entityManager.find(type, id);
	}

	@Transactional
	public T update(T object) {
		return entityManager.merge(object);
	}

	@Transactional
	public T save(T object) {
		if ( object.getId() == null ) {
			object = create(object);
		} else {
			object = update(object);
		}
		return object;
	}
	
	@Transactional
	public void delete(int id) {
		T ref = entityManager.getReference(type, id);
		entityManager.remove(ref);
	}
	
	public void flush() {
		entityManager.flush();
	}
	
	public void commit() {
		entityManager.getTransaction().commit();
	}
	
	@Transactional
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
	
	protected EntityManager getEntityManager() {
		return entityManager;
	}
}
