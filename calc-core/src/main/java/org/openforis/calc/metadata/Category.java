package org.openforis.calc.metadata;

/**
 * Represents one possible value of a {@link CategoricalVariable}.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class Category {
	private Integer id;
	private String code;
	private String name;
	private String caption;
	private String description;
	private boolean overrideInputMetadata;
	private int index;
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