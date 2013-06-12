package org.openforis.calc.common;

/**
 * An object uniquely identifiable by an Integer id. Used for persistence, the id is null when the object is new and not yet persisted.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public interface Identifiable {

	Integer getId();
	
	void setId(Integer id);
}