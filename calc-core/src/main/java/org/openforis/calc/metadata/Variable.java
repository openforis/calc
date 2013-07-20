package org.openforis.calc.metadata;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openforis.calc.common.UserObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for Calc variables.  Variables may be either categorical or
 * quantitative.  Note that binary classes are special cases of categorical
 * variables which accept TRUE, FALSE and NA values.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "variable")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Variable extends UserObject {
	private String caption;
	private boolean cubeMember;
	private int index;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "entity_id")
	@JsonIgnore
	private Entity entity;
	private Scale scale;
	private String valueColumn;

	public org.openforis.calc.metadata.Variable.Type getType() {
		throw new UnsupportedOperationException();
	}

	public Entity getEntity() {
		return this.entity;
	}

	public void setScale(org.openforis.calc.metadata.Variable.Scale scale) {
		this.scale = scale;
	}

	public org.openforis.calc.metadata.Variable.Scale getScale() {
		return this.scale;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void setCubeMember(boolean cubeMember) {
		this.cubeMember = cubeMember;
	}

	public boolean isCubeMember() {
		return this.cubeMember;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}

	public enum Type {
		QUANTITATIVE, CATEGORICAL;
	}

	public enum Scale {
		NOMINAL, ORDINAL, BINARY, RATIO, INTERVAL, OTHER;

		public org.openforis.calc.metadata.Variable.Type getType() {
			throw new UnsupportedOperationException();
		}
	}
}