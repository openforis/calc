/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.List;

/**
 * @author M. Togna
 * 
 */
public class Hierarchy {

	private String name;
	private List<Level> levels;
	private View view;
	private Table table;

	Hierarchy(String name) {
		this.name = name;
		this.levels = new ArrayList<Level>();
	}

	public String getName() {
		return name;
	}

	public List<Level> getLevels() {
		return levels;
	}

	void addLevel(Level level) {
		levels.add(level);
	}
	
	void addLevel(int index, Level level) {
		levels.add(index, level);
	}
	
	public View getView() {
		return view;
	}

	void setView(View view) {
		this.view = view;
	}

	public Table getTable() {
		return table;
	}

	void setTable(Table table) {
		this.table = table;
	}

	public static class Level {
		private String name;
		private String column;
		private String nameColumn;
		private String caption;

		Level(String name, String column, String nameColumn, String caption) {
			this.name = name;
			this.column = column;
			this.nameColumn = nameColumn;
			this.caption = caption;
		}

		public String getName() {
			return name;
		}

		public String getColumn() {
			return column;
		}

		public String getNameColumn() {
			return nameColumn;
		}
		
		public String getCaption() {
			return caption;
		}
	}

	public static class Table {
		private String schema;
		private String name;

		public Table(String schema, String name) {
			super();
			this.schema = schema;
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getSchema() {
			return schema;
		}
	}

	public static class View {
		private String alias;
		private String sql;

		public View(String alias, String sql) {
			super();
			this.alias = alias;
			this.sql = sql;
		}

		public String getAlias() {
			return alias;
		}

		public String getSql() {
			return sql;
		}

	}

}
