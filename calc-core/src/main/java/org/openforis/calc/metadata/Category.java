package org.openforis.calc.metadata;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents one possible value of a {@link CategoricalVariable}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@javax.persistence.Entity
@Table(name = "category")
public class Category {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	@Column(name = "code")
	private String code;
	@Column(name = "name")
	private String name;
	@Column(name = "caption")
	private String caption;
	@Column(name = "description")
	private String description;
	@Column(name = "override")
	private boolean overrideInputMetadata;
	@Column(name = "sort_order") //TODO check column/variable name
	private int index;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variable_id")
	@JsonIgnore
	private CategoricalVariable variable;

	public CategoricalVariable getVariable() {
		return this.variable;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return this.caption;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setOverrideInputMetadata(boolean overrideInputMetadata) {
		this.overrideInputMetadata = overrideInputMetadata;
	}

	public boolean isOverrideInputMetadata() {
		return this.overrideInputMetadata;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}
}