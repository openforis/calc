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

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "entity_id")
	private Entity entity;

	@JsonIgnore	
	@Enumerated(EnumType.STRING)
	@Column(name = "scale")
	private Scale scale;

	@JsonIgnore	
	@Column(name = "input_value_column")
	private String inputValueColumn;

	@JsonIgnore	
	@Column(name = "output_value_column")
	private String outputValueColumn;

	@JsonIgnore	
	@Column(name = "dimension_table")
	private String dimensionTable;

	@JsonIgnore	
	@Column(name = "dimension_table_id_column")
	private String dimensionTableIdColumn;

	@JsonIgnore	
	@Column(name = "override")
	private boolean override;
	
	@JsonIgnore	
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

	@JsonIgnore
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
	
	public boolean isUserDefined() {
		return originalId == null;
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
				+ ((dimensionTableIdColumn == null) ? 0
						: dimensionTableIdColumn.hashCode());
		result = prime * result + ((entity == null) ? 0 : entity.hashCode());
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
		result = prime * result + sortOrder;
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
		if (dimensionTableIdColumn == null) {
			if (other.dimensionTableIdColumn != null)
				return false;
		} else if (!dimensionTableIdColumn.equals(other.dimensionTableIdColumn))
			return false;
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity))
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
		if (sortOrder != other.sortOrder)
			return false;
		return true;
	}

	public String getDimensionTableIdColumn() {
		return dimensionTableIdColumn;
	}

	public void setDimensionTableIdColumn(String dimensionTableIdColumn) {
		this.dimensionTableIdColumn = dimensionTableIdColumn;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result
//				+ ((dimensionTable == null) ? 0 : dimensionTable.hashCode());
//		result = prime
//				* result
//				+ ((inputValueColumn == null) ? 0 : inputValueColumn.hashCode());
//		result = prime * result
//				+ ((originalId == null) ? 0 : originalId.hashCode());
//		result = prime
//				* result
//				+ ((outputValueColumn == null) ? 0 : outputValueColumn
//						.hashCode());
//		result = prime * result + (override ? 1231 : 1237);
//		result = prime * result + ((scale == null) ? 0 : scale.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (!super.equals(obj))
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Variable<?> other = (Variable<?>) obj;
//		if (dimensionTable == null) {
//			if (other.dimensionTable != null)
//				return false;
//		} else if (!dimensionTable.equals(other.dimensionTable))
//			return false;
//		if (inputValueColumn == null) {
//			if (other.inputValueColumn != null)
//				return false;
//		} else if (!inputValueColumn.equals(other.inputValueColumn))
//			return false;
//		if (originalId == null) {
//			if (other.originalId != null)
//				return false;
//		} else if (!originalId.equals(other.originalId))
//			return false;
//		if (outputValueColumn == null) {
//			if (other.outputValueColumn != null)
//				return false;
//		} else if (!outputValueColumn.equals(other.outputValueColumn))
//			return false;
//		if (override != other.override)
//			return false;
//		if (scale != other.scale)
//			return false;
//		return true;
//	}
	
	
}
