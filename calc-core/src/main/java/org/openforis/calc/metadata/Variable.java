package org.openforis.calc.metadata;

import org.openforis.calc.persistence.jooq.tables.pojos.VariableBase;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for Calc variables.  Variables may be either categorical or quantitative.  Note that binary classes are special cases of categorical
 * variables which accept TRUE, FALSE and NA values.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public abstract class Variable<T> extends VariableBase {
	
	private static final long serialVersionUID = 1L;

	public enum Type {
		QUANTITATIVE, CATEGORICAL, BINARY, TEXT;
	}

	public enum Scale {
		NOMINAL, ORDINAL, BINARY, RATIO, INTERVAL, OTHER, TEXT;
	}

	public Variable() {
		//default values
		setDisaggregate(false);
		setDegenerateDimension(false);
		setOverride(false);
	}
	
	@JsonIgnore
	private Entity entity;

	public abstract Variable.Type getType();

	public Entity getEntity() {
		return this.entity;
	}

	void setEntity(Entity entity) {
		this.entity = entity;
		if( entity != null ) {
			setEntityId( entity.getId() );
		}
	}

	@JsonIgnore
	public boolean isInput() {
		return getInputValueColumn() != null;
	}

	public boolean isUserDefined() {
		return getOriginalId() == null;
	}
	
	public abstract T getDefaultValueTemp();

	public abstract void setDefaultValue(T defaultValue);

	@Override
	public int hashCode() {
		Integer id = getId();
		String name = getName();
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		Integer id = getId();
		String name = getName();
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable<?> other = (Variable<?>) obj;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!id.equals(other.getId()))
			return false;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		return true;
	}
	
}
