package org.openforis.calc.server.rest;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author G. Miceli
 *
 * @param <T>
 */
public class SubResource<T> extends Resource {
	
	@Autowired
	private RootResource rootResource;

	private T key;

	public T getKey() {
		return key;
	}
	
	void setKey(T key) {
		this.key = key;
	}

	String[] getFields() {
		return rootResource.getFields();
	}
}
