package org.openforis.calc.common;

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
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
}