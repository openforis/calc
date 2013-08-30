package org.openforis.calc.common;

import javax.persistence.Column;

/**
 * 
 * @author G. Miceli
 *
 */
public abstract class NamedUserObject extends UserObject {
	
	@Column(name = "name")
	private String name;


	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
