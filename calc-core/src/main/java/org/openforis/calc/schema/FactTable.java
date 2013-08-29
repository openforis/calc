/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author G. Miceli
 * 
 */
public class FactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_fact";
	private static final String DIMENSION_ID_COLUMN_FORMAT = "%s_id";

	private Map<VariableAggregate, Field<BigDecimal>> measureFields;
	private Map<CategoricalVariable, Field<Integer>> dimensionIdFields;
	private Map<AoiHierarchy, List<AggregateTable>> aggregateTables;
	
	protected FactTable(Entity entity, String name, Schema schema, DataTable sourceTable, DataTable parentTable) {
		super(entity, name, schema, sourceTable, parentTable);
	}

	FactTable(Entity entity, Schema schema, OutputDataTable sourceTable, DataTable parentTable) {
		this(entity, getName(entity), schema, sourceTable, parentTable);
		createPrimaryKeyField();
		createDimensionFields(entity);
		createStratumIdField();
		createAoiIdFields(null);
		createQuantityFields(false);
		createMeasureFields(entity);
		createParentIdField();
		createAggregateTables();
	}

	/**
	 * Recursively up to root unit of analysis
	 */
	protected void createDimensionFields(Entity entity) {
		Entity parent = entity.getParent();
		if ( parent != null && parent.isUnitOfAnalysis() ) {
			createDimensionFields(parent);
		}
		createDimensionIdFields(entity);
		createCategoryValueFields(entity, false, true);
	}

	private void createDimensionIdFields(Entity entity) {
		this.dimensionIdFields = new HashMap<CategoricalVariable, Field<Integer>>();
		List<Variable> variables = entity.getVariables();
		for ( Variable var : variables ) {
			if ( var instanceof CategoricalVariable ) {
				createDimensionIdField((CategoricalVariable) var);
			}
		}
	}

	private void createDimensionIdField(CategoricalVariable var) {
		if ( !var.isDegenerateDimension() && var.isDisaggregate() ) {
			String fieldName = String.format(DIMENSION_ID_COLUMN_FORMAT, var.getName());
			Field<Integer> fld = createField(fieldName, SQLDataType.INTEGER, this);
			dimensionIdFields.put(var, fld);
		}
	}

	protected void createMeasureFields(Entity entity) {
		this.measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
		List<VariableAggregate> aggregates = entity.getVariableAggregates();
		for ( VariableAggregate agg : aggregates ) {
			Field<BigDecimal> field = createField(agg.getName(), Psql.DOUBLE_PRECISION, this);
			measureFields.put(agg, field);
		}
	}
	
	protected void createAggregateTables() {
		this.aggregateTables = new HashMap<AoiHierarchy, List<AggregateTable>>();
		Entity entity = getEntity();
	
		if ( entity.isSamplingUnit() ) {
			Workspace workspace = entity.getWorkspace();
			
			for ( AoiHierarchy aoiHierarchy : workspace.getAoiHierarchies() ) {
				for ( AoiHierarchyLevel level : aoiHierarchy.getLevels() ) {
					AggregateTable aggregateTable = new AggregateTable(this, level);
					addAggregateTable(aoiHierarchy, aggregateTable);
				}
			}
		}
	}
	
	private void addAggregateTable(AoiHierarchy aoiHierarchy, AggregateTable aggregateTable) {
		List<AggregateTable> aggTables = this.aggregateTables.get(aoiHierarchy);
		if( aggTables == null ){
			aggTables = new ArrayList<AggregateTable>();
			this.aggregateTables.put(aoiHierarchy, aggTables);
		}
		aggTables.add(aggregateTable);
	}

	private static String getName(Entity entity) {
		return String.format(TABLE_NAME_FORMAT, entity.getDataTable());
	}

	public Field<BigDecimal> getMeasureField(VariableAggregate aggregate) {
		return measureFields.get(aggregate);
	}

	public Field<Integer> getDimensionIdField(CategoricalVariable variable) {
		return dimensionIdFields.get(variable);
	}

	public Collection<Field<Integer>> getDimensionIdFields() {
		return Collections.unmodifiableCollection(dimensionIdFields.values());
	}
	
	public List<AggregateTable> getAggregateTables(AoiHierarchy aoiHierarchy) {
		return CollectionUtils.unmodifiableList( aggregateTables.get(aoiHierarchy) );
	}
	
}
