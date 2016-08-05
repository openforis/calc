/**
 * 
 */
package org.openforis.calc.system;

import java.util.List;

import org.openforis.calc.persistence.jooq.tables.SystemPropertyTable;
import org.openforis.calc.persistence.jooq.tables.daos.SystemPropertyDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 *
 */
@Repository
public class SystemPropertyManager {

	@Autowired
	private SystemPropertyDao systemPropertyDao;

	@Transactional
	public List<SystemProperty> getAll() {
		List<SystemProperty> list = systemPropertyDao.findAll();
		return list;
	}
	
	@Transactional
	public void save(String name , String value) {
		SystemProperty systemProperty = getSystemPropertyByName(name);
		
		if( systemProperty == null ){
			systemProperty = new SystemProperty();
			systemProperty.setName(name);
			systemProperty.setType(SystemProperty.TYPE.STRING);
		}
		
		systemProperty.setValue(value);
		
		save(systemProperty);
	}
	
	@Transactional
	public void save(SystemProperty systemProperty) {
		if (systemPropertyDao.exists(systemProperty)) {
			systemPropertyDao.update(systemProperty);
		} else {
			systemPropertyDao.insert(systemProperty);
		}
	}
	
	@Transactional
	public SystemProperty getSystemPropertyByName(String name){
		return systemPropertyDao.fetchOne(SystemPropertyTable.SYSTEM_PROPERTY.NAME, name);
	}
	

}
