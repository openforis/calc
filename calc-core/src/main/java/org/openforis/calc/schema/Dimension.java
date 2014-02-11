package org.openforis.calc.schema;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * @author M. Togna
 */
public class Dimension extends Member {

	protected String name;
	protected String caption;
	protected Hierarchy hierarchy;

	Dimension(RolapSchema rolapSchema) {
		super(rolapSchema);
	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	void setHierarchy(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = capitalize( name );
	}

	public String getCaption() {
		return caption;
	}

	void setCaption(String caption) {
		this.caption = caption;
	}

}
