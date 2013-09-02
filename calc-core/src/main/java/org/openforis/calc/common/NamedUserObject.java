package org.openforis.calc.common;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 
 * @author G. Miceli
 *
 */
@MappedSuperclass
public abstract class NamedUserObject extends UserObject {
	
	@Column(name = "name")
	private String name;


	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
	
	
	@Override
	public String toString() {
		return String.format("%s [%s]", name, getId());
	}
}
