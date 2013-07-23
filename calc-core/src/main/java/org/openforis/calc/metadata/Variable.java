package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorFormula;
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
//@DiscriminatorColumn(name = "type")
//@DiscriminatorFormula("case when scale in ('RATIO','INTERVAL','OTHER') then 'Q' when scale='BINARY' then 'B' else 'C' end")
@DiscriminatorFormula("case when scale in ('RATIO','INTERVAL','OTHER') then 'Q' else 'C' end")
public abstract class Variable extends UserObject {
	@Column(name = "caption")
	private String caption;
	
	@Column(name = "cube_member")
	private boolean cubeMember;
	
	@Column(name = "sort_order")
	private int sortOrder;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "entity_id")
	@JsonIgnore
	private Entity entity;

	@Column(name = "scale")
	private Scale scale;
	
	@Column(name = "value_column")
	private String valueColumn;

	public enum Type {
		QUANTITATIVE, CATEGORICAL, BINARY;
	}

	public enum Scale {
		NOMINAL, ORDINAL, BINARY, RATIO, INTERVAL, OTHER;
	}

	public abstract Variable.Type getType();

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

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getValueColumn() {
		return valueColumn;
	}

	public void setValueColumn(String valueColumn) {
		this.valueColumn = valueColumn;
	}
	
	void setEntity(Entity entity) {
		this.entity = entity;
	}
}