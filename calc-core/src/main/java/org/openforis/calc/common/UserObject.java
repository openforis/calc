package org.openforis.calc.common;

import javax.persistence.Column;

/**
 * Base class for user-controlled objects.  Since this base class also defines
 * mutator methods, is for use only by persistable objects whose name and
 * description may be changed at run-time.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class UserObject implements Identifiable {
	private Integer id;
	private String name;
	private String description;

	@Override
	@Column(name = "id")
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Column(name = "name")
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Column(name = "description")
	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
	
	@Override
	public String toString() {
		return name + " [" + id + "]";
	}
}