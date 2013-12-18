/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.VariableAggregate;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 */
public class NewFactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_fact";
	private static final String DIMENSION_ID_COLUMN_FORMAT = "%s_id";

	protected Map<VariableAggregate, Field<BigDecimal>> measureFields;
	protected Map<CategoricalVariable<?>, Field<Integer>> dimensionIdFields;
	
	private Field<Integer> stratumIdField;
	private NewFactTable parentTable;
//	private Map<AoiHierarchy, List<AggregateTable>> aggregateTables;
	private EntityDataView entityView;
private TableField<Record, BigDecimal> plotAreaField;

//	protected NewFactTable(Entity entity, String name, Schema schema, DataTable sourceTable, NewFactTable parentTable) {
//		super(entity, name, schema);
//	}
	
	NewFactTable(Entity entity, Schema schema, EntityDataView entityView, NewFactTable parentTable) {
		super(entity, getName(entity), schema);
		this.entityView = entityView;
		TableField<Record, BigDecimal> plotArea = entityView.getPlotAreaField();
		if (plotArea != null) {
			this.plotAreaField = super.createField(plotArea.getName(), Psql.DOUBLE_PRECISION, this);
		}
//		this.sourceOutputTable = sourceOutputTable;
//		this.parentTable = parentTable;
//		
		this.dimensionIdFields = new HashMap<CategoricalVariable<?>, Field<Integer>>();
		this.measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
//		this.aggregateTables = new HashMap<AoiHierarchy, List<AggregateTable>>();
//		
		createPrimaryKeyField();
		createDimensionFieldsRecursive(entity);
//		createStratumIdField();
//		createAoiIdFields();
		createQuantityFields(false, true);
//		createMeasureFields(entity);
//		createParentIdField();
//		createAggregateTables();
	}

//	/**
//	 * Recursively up to root unit of analysis
//	 */
	protected void createDimensionFieldsRecursive(Entity entity) {
		Entity parent = entity.getParent();
		// it stops if the entity is the sampling unit. cannot aggretate at higher level
		if ( parent != null && !entity.isSamplingUnit() ) {
			createDimensionFieldsRecursive(parent);
		}
		createDimensionIdFields(entity);
		createCategoryValueFields(entity, false);
	}

	private void createDimensionIdFields(Entity entity) {
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable<?> var : variables ) {
			createDimensionIdField(var);
		}
	}

	protected void createDimensionIdField(CategoricalVariable<?> var) {
		if ( !var.isDegenerateDimension() && var.isDisaggregate() ) {
			String fieldName = String.format(DIMENSION_ID_COLUMN_FORMAT, var.getName());
			Field<Integer> fld = createField(fieldName, SQLDataType.INTEGER, this);
			dimensionIdFields.put(var, fld);
		}
	}

	protected void createMeasureFields(Entity entity) {
		List<VariableAggregate> aggregates = entity.getVariableAggregates();
		for ( VariableAggregate agg : aggregates ) {
			Field<BigDecimal> field = createField(agg.getName(), Psql.DOUBLE_PRECISION, this);
			measureFields.put(agg, field);
		}
	}
//	
//	protected void createAggregateTables() {
//		Entity entity = getEntity();
//	
//		if ( entity.isSamplingUnit() ) {
//			Workspace workspace = entity.getWorkspace();
//			
//			for ( AoiHierarchy aoiHierarchy : workspace.getAoiHierarchies() ) {
//				for ( AoiLevel level : aoiHierarchy.getLevels() ) {
//					AggregateTable aggregateTable = new AggregateTable(this, level);
//					addAggregateTable(aoiHierarchy, aggregateTable);
//				}
//			}
//		}
//	}
//	
//	private void addAggregateTable(AoiHierarchy aoiHierarchy, AggregateTable aggregateTable) {
//		List<AggregateTable> aggTables = this.aggregateTables.get(aoiHierarchy);
//		if( aggTables == null ){
//			aggTables = new ArrayList<AggregateTable>();
//			this.aggregateTables.put(aoiHierarchy, aggTables);
//		}
//		aggTables.add(aggregateTable);
//	}
//
	private static String getName(Entity entity) {
		return String.format(TABLE_NAME_FORMAT, entity.getDataTable());
	}
//
//	public Field<BigDecimal> getMeasureField(VariableAggregate aggregate) {
//		return measureFields.get(aggregate);
//	}
//
//	public Field<Integer> getDimensionIdField(CategoricalVariable<?> variable) {
//		return dimensionIdFields.get(variable);
//	}
//
	public Collection<Field<Integer>> getDimensionIdFields() {
		return Collections.unmodifiableCollection(dimensionIdFields.values());
	}
//	
//	protected void createStratumIdField() {
//		this.stratumIdField = createField("_stratum_id", INTEGER, this);
//	}
//
//	public Field<Integer> getStratumIdField() {
//		return stratumIdField;
//	}
//
//	public OutputTable getSourceOutputTable() {
//		return sourceOutputTable;
//	}
//
//	public NewFactTable getParentTable() {
//		return parentTable;
//	}
//
//	public List<AggregateTable> getAggregateTables(AoiHierarchy aoiHierarchy) {
//		return CollectionUtils.unmodifiableList( aggregateTables.get(aoiHierarchy) );
//	}
//
//	public Collection<AggregateTable> getAggregateTables() {
//		Collection<AggregateTable> tables = new ArrayList<AggregateTable>();
//		for (List<AggregateTable> aggTables : aggregateTables.values()) {
//			tables.addAll(aggTables);
//		}
//		return Collections.unmodifiableCollection(tables);
//	}

	public TableField<Record, BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}
	
	public EntityDataView getEntityView() {
		return entityView;
	}

}
