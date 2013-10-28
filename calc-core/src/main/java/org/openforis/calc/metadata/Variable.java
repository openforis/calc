package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DiscriminatorFormula;
import org.openforis.calc.common.NamedUserObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for Calc variables.  Variables may be either categorical or quantitative.  Note that binary classes are special cases of categorical
 * variables which accept TRUE, FALSE and NA values.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "variable")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("case when scale='TEXT' then 'T' when scale in ('RATIO','INTERVAL','OTHER') then 'Q' when scale='BINARY' then 'B' else 'C' end")
public abstract class Variable<T> extends NamedUserObject {
	
	public enum Type {
		QUANTITATIVE, CATEGORICAL, BINARY, TEXT;
	}

	public enum Scale {
		NOMINAL, ORDINAL, BINARY, RATIO, INTERVAL, OTHER, TEXT;
	}

	@JsonIgnore
	@Column(name = "sort_order")
	private int sortOrder;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_id")
	@JsonIgnore
	private Entity entity;

	@Enumerated(EnumType.STRING)
	@Column(name = "scale")
	private Scale scale;

	@Column(name = "input_value_column")
	private String inputValueColumn;

	@Column(name = "output_value_column")
	private String outputValueColumn;

	@Column(name = "dimension_table")
	private String dimensionTable;

	@Column(name = "override")
	private boolean override;
	
	@Column(name = "original_id")
	private Integer originalId;

	public abstract Variable.Type getType();

	public Entity getEntity() {
		return this.entity;
	}

	void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Scale getScale() {
		return this.scale;
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getInputValueColumn() {
		return inputValueColumn;
	}

	public void setInputValueColumn(String inputValueColumn) {
		this.inputValueColumn = inputValueColumn;
	}

	public String getOutputValueColumn() {
		return outputValueColumn;
	}

	public void setOutputValueColumn(String outputValueColumn) {
		this.outputValueColumn = outputValueColumn;
	}

	public String getDimensionTable() {
		return dimensionTable;
	}

	public void setDimensionTable(String dimensionTable) {
		this.dimensionTable = dimensionTable;
	}

	public boolean isInput() {
		return inputValueColumn != null;
	}

	public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	public Integer getOriginalId() {
		return originalId;
	}
	
	public void setOriginalId(Integer originalId) {
		this.originalId = originalId;
	}
	
	public abstract T getDefaultValue();

	public abstract void setDefaultValue(T defaultValue);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((dimensionTable == null) ? 0 : dimensionTable.hashCode());
		result = prime
				* result
				+ ((inputValueColumn == null) ? 0 : inputValueColumn.hashCode());
		result = prime * result
				+ ((originalId == null) ? 0 : originalId.hashCode());
		result = prime
				* result
				+ ((outputValueColumn == null) ? 0 : outputValueColumn
						.hashCode());
		result = prime * result + (override ? 1231 : 1237);
		result = prime * result + ((scale == null) ? 0 : scale.hashCode());
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
		Variable<?> other = (Variable<?>) obj;
		if (dimensionTable == null) {
			if (other.dimensionTable != null)
				return false;
		} else if (!dimensionTable.equals(other.dimensionTable))
			return false;
		if (inputValueColumn == null) {
			if (other.inputValueColumn != null)
				return false;
		} else if (!inputValueColumn.equals(other.inputValueColumn))
			return false;
		if (originalId == null) {
			if (other.originalId != null)
				return false;
		} else if (!originalId.equals(other.originalId))
			return false;
		if (outputValueColumn == null) {
			if (other.outputValueColumn != null)
				return false;
		} else if (!outputValueColumn.equals(other.outputValueColumn))
			return false;
		if (override != other.override)
			return false;
		if (scale != other.scale)
			return false;
		return true;
	}
	
}
