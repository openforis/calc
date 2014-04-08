package org.openforis.calc.common;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * 
 * @author G. Miceli
 *
 */
@MappedSuperclass
public abstract class NamedUserObject extends UserObject {
	
	public static void sortByName(List<? extends NamedUserObject> objects) {
		Collections.sort(objects, new Comparator<NamedUserObject>() {
			@Override
			public int compare(NamedUserObject o1, NamedUserObject o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
	}
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedUserObject other = (NamedUserObject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
