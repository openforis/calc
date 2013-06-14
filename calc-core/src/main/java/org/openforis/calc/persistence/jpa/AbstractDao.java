package org.openforis.calc.persistence.jpa;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.common.ReflectionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
@Repository
public abstract class AbstractDao<T extends Identifiable> {
	
    @PersistenceContext
    private EntityManager entityManager;

    private Class<T> type;
    
    protected AbstractDao() {
//       this.type = ReflectionUtils.extractGenericType(getClass());
	}
    
    @Transactional
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

	@Transactional
	public T save(T object) {
		if ( object.getId() == null ) {
//			return create(object);
			object = create(object);
		} else {
			object = update(object);
		}
//		entityManager.refresh(object);
		return object;
	}
	
	public void delete(int id) {
		T ref = entityManager.getReference(type, id);
		entityManager.remove(ref);
	}
	
	@PostConstruct
	void initType(){
		this.type = ReflectionUtils.extractGenericType(getClass());
	}
}
