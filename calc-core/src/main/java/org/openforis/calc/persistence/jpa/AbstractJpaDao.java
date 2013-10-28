package org.openforis.calc.persistence.jpa;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.common.Identifiable;
import org.openforis.calc.common.Reflection;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * @param <T>
 */
@Repository
public abstract class AbstractJpaDao<T extends Identifiable> {
	
    @PersistenceContext
    private EntityManager entityManager;

    private Class<T> type;
    
    protected AbstractJpaDao() {
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
		return loadAll(null);
	}
	
	@Transactional
	public List<T> loadAll(String orderBy) {
		String select = "select a from "+type.getSimpleName()+" a";
		if( StringUtils.isNotBlank(orderBy) ){
			select += " order by " + orderBy;
					 
		}
		TypedQuery<T> q = entityManager.createQuery(select, type);
		q.setHint("org.hibernate.cacheable", true);

		List<T> list = q.getResultList();
		return list;
	}
	
	@Transactional
	public T fetchFirst(String property, Object value) {
		List<T> results = fetch(property, value);
		if ( results != null && ! results.isEmpty() ) {
			return results.get(0);
		} else {
			return null;
		}
	}
	
	@Transactional
	public List<T> fetch(String property, Object value) {
		EntityManager em = getEntityManager();
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> q = cb.createQuery(type);
		
		Root<T> root = q.from(type);
		q.where(cb.equal(root.get(property), value));
		
		List<T> results = em.createQuery(q).getResultList();
		return results;
	}
	
	@PostConstruct
	void initType(){
		this.type = Reflection.extractGenericType(getClass());
	}
	
	protected EntityManager getEntityManager() {
		return entityManager;
	}
}
