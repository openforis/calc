/**
 * 
 */
package org.openforis.calc.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.Field;

/**
 * @author M. Togna
 * 
 */
public class Table {

	private List<AggName> aggNames;
	private String schema;
	private String name;
	private Cube cube;

	Table(String name, String schema, Cube cube) {
		this.name = name;
		this.schema = schema;
		this.cube = cube;

		createAggNames();
	}

	public String getSchema() {
		return schema;
	}

	public String getName() {
		return name;
	}

	public List<AggName> getAggNames() {
		return aggNames;
	}

	private void createAggNames() {
		this.aggNames = new ArrayList<Table.AggName>();
		if ( cube.getFactTable().isGeoreferenced() ) {
			Collection<AggregateTable> aggregateTables = cube.getFactTable().getAggregateTables();
			for ( AggregateTable aggregateTable : aggregateTables ) {
				AggName aggName = createAggName(aggregateTable);
				this.aggNames.add(aggName);
			}
		}

	}

	private AggName createAggName(AggregateTable aggregateTable) {
		AggName aggName = new AggName();
		
		return aggName;
	}

	public static class AggName {

		private String aggFactCount;
		private List<AggForeignKey> aggForeignKeys;
		private List<AggMeasure> aggMeasures;
		private List<AggLevel> aggLevels;

		public AggName() {

		}

		public String getAggFactCount() {
			return aggFactCount;
		}

		public List<AggForeignKey> getAggForeignKeys() {
			return aggForeignKeys;
		}

		public List<AggMeasure> getAggMeasures() {
			return aggMeasures;
		}

		public List<AggLevel> getAggLevels() {
			return aggLevels;
		}
	}

	public static class AggForeignKey {
		private String factColumn;
		private String aggColumn;

		public AggForeignKey(String factColumn, String aggColumn) {
			this.factColumn = factColumn;
			this.aggColumn = aggColumn;
		}

		public String getFactColumn() {
			return factColumn;
		}

		public String getAggColumn() {
			return aggColumn;
		}

	}

	public static class AggMeasure {
		private String column;
		private String name;

		public AggMeasure(String column, String name) {
			this.column = column;
			this.name = name;
		}

		public String getColumn() {
			return column;
		}

		public String getName() {
			return name;
		}

	}

	public static class AggLevel {
		private String column;
		private String name;

		public AggLevel(String column, String name) {
			this.column = column;
			this.name = name;
		}

		public String getColumn() {
			return column;
		}

		public String getName() {
			return name;
		}

	}

}
