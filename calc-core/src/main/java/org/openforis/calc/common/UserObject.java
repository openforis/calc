package org.openforis.calc.common;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * Base class for user-controlled objects.  Since this base class also defines
 * mutator methods, is for use only by persistable objects whose name and
 * description may be changed at run-time.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
// TODO better name?
@MappedSuperclass
public abstract class UserObject extends AbstractNamedIdentifiable {
	@Column(name = "description")
	private String description;

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
}