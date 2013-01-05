/**
 * 
 */
package org.openforis.calc.persistence.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * 
 */
@Transactional
public abstract class AbstractDAO {

	@PersistenceContext	
	private EntityManager entityManager;
	
//	@Autowired
//	private LocalContainerEntityManagerFactoryBean entityManagerFactory;
	
	@Autowired
	private DataSource dataSource;
	
	protected EntityManager getEntityManager() {
		return entityManager;
	}
	
//	protected LocalContainerEntityManagerFactoryBean getEntityManagerFactory() {
//		return entityManagerFactory;
//	}
	
	protected DataSource getDataSource() {
		return dataSource;
	}
	
	protected void persist(Object entity) {
		entityManager.persist(entity);
	}

	protected void remove(Object entity) {
		entityManager.remove(entity);
	}

	protected <T> T find(Class<T> entityClass, Object primaryKey) {
		return entityManager.find(entityClass, primaryKey);
	}

	protected void refresh(Object entity) {
		entityManager.refresh(entity);
	}

	protected Query createQuery(String qlString) {
		return entityManager.createQuery(qlString);
	}

	protected Query createNamedQuery(String name) {
		return entityManager.createNamedQuery(name);
	}

	protected Query createNativeQuery(String sqlString) {
		return entityManager.createNativeQuery(sqlString);
	}

	protected Query createNativeQuery(String sqlString, @SuppressWarnings("rawtypes") Class resultClass) {
		return entityManager.createNativeQuery(sqlString, resultClass);
	}

	protected Query createNativeQuery(String sqlString, String resultSetMapping) {
		return entityManager.createNativeQuery(sqlString, resultSetMapping);
	}

	protected boolean isOpen() {
		return entityManager.isOpen();
	}

	protected EntityTransaction getTransaction() {
		return entityManager.getTransaction();
	}
	
	@Transactional
	public int executeNativeUpdate(String sqlQuery){
		Query query = entityManager.createNativeQuery(sqlQuery);
		return query.executeUpdate();
	}
	
	
//	public void setEntityManager(EntityManager entityManager) {
//		this.entityManager = entityManager;
//	}
}
