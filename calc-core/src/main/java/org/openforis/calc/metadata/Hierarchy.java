package org.openforis.calc.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a categorical variable hierarchy, made up of one or more hierarchy {@link Level}s.
 * 
 * @author G. Miceli
 * @author M. Togna
 */
public final class Hierarchy {
	private Integer id;
	private String caption;
	private String description;
	private CategoricalVariable variable;
	private List<Level> levels = new ArrayList<Level>();

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
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
	
	public CategoricalVariable getVariable() {
		return variable;
	}
	
	public List<Level> getLevels() {
		return Collections.unmodifiableList(levels);
	}
	/**
	 * Defines a single level of a hierarchy of (@link Category}s.
	 */
	public final class Level {
		private Integer id;
		private String caption;
		private String description;
		private int rank;
		private Hierarchy hierarchy;

		public void setId(Integer id) {
			this.id = id;
		}

		public Integer getId() {
			return this.id;
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

		public int getRank() {
			return this.rank;
		}
		
		public Hierarchy getHierarchy() {
			return hierarchy;
		}
	}
}