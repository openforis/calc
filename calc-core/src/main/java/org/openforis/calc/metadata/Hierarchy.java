package org.openforis.calc.metadata;

import java.util.ArrayList;

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
	private ArrayList<org.openforis.calc.metadata.Hierarchy.Level> levels = new ArrayList<Level>();

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
	}
}