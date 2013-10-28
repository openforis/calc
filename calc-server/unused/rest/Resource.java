package org.openforis.calc.server.rest;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


/**
 * @author G. Miceli
 */
public abstract class Resource implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	public <K, R extends SubResource<K>> R getResource(Class<R> type, K key) {
		R rsc = applicationContext.getBean(type);
		rsc.setKey(key);
		return rsc;
	}
	
	public <K, R extends SubResource<K>> R getResource(Class<R> type) {
		return getResource(type, null);
	}
}
