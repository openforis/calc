package org.openforis.calc.server.rest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author G. Miceli
 */
public abstract class CalcResource<T> implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	private T key;
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public <K, R extends CalcResource<K>> R getResource(Class<R> type, K key) {
		R rsc = applicationContext.getBean(type);
		rsc.key = key;
		return rsc;
	}

	public T getKey() {
		return key;
	}
}
