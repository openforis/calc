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
@DiscriminatorFormula("case when scale in ('RATIO','INTERVAL','OTHER') then 'Q' when scale='BINARY' then 'B' else 'C' end")
public abstract class Variable extends NamedUserObject {
	@Column(name = "sort_order")
	private int sortOrder;

	@ManyToOne(fetch = FetchType.LAZY)
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
	private Boolean override;

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

	public Boolean isOverride() {
		return override;
	}

	public void setOverride(Boolean override) {
		this.override = override;
	}
}
