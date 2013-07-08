package org.openforis.calc.engine;

import javax.annotation.PostConstruct;

import org.openforis.calc.common.Identifiable;
import org.openforis.calc.persistence.jpa.AbstractDao;

//TODO
public abstract class AbstractManager<T extends Identifiable> {

	private AbstractDao<T> dao;
	
	protected AbstractManager(){
		
	}
	
	protected void setDao(AbstractDao<T> dao) {
		this.dao = dao;
	}
	
	@PostConstruct
	abstract void initDao();
}
