package org.openforis.calc.metadata;

/**
 * Collects a set of {@link Category}s together into a user-defined grouping.  Non-leaf groups contain other Groups, which leaves contain actual Categories.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class Group {
	private Integer id;
	private String code;
	private String name;
	private String caption;
	private String description;
	private int index;
	private Hierarchy hierarchy;
	private Hierarchy.Level level;

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

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}
	
	public Hierarchy getHierarchy() {
		return hierarchy;
	}
	
	public Hierarchy.Level getLevel() {
		return level;
	}
}