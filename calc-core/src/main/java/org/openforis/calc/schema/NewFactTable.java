/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 */
public class NewFactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_fact";
//	private static final String DIMENSION_ID_COLUMN_FORMAT = "%s_id";

//	private NewFactTable parentTable;
	private Map<AoiLevel, AoiAggregateTable> aoiAggregateTables;
	private SamplingUnitAggregateTable samplingUnitAggregateTable;
	
	private EntityDataView entityView;
	private Field<BigDecimal> plotAreaField;
	private InputSchema schema;

//	protected NewFactTable(Entity entity, String name, Schema schema, DataTable sourceTable, NewFactTable parentTable) {
//		super(entity, name, schema);
//	}
//	NewFactTable(Entity entity , String name, InputSchema schema){
//		super(entity, name, schema);
//	}
	NewFactTable(Entity entity, InputSchema schema) {
		this( entity, getName(entity), schema );
	}
	
	NewFactTable(Entity entity, String name, InputSchema schema) {
		super(entity, name, schema);
		this.schema = schema;
		this.entityView = schema.getDataView(entity);

		initFields();
	}

	protected void initFields() {
		Entity entity = entityView.getEntity();	
		TableField<Record, BigDecimal> plotArea = entityView.getPlotAreaField();
		
		if (plotArea != null) {
			this.plotAreaField = super.createField(plotArea.getName(), Psql.DOUBLE_PRECISION, this);
		}
//		this.sourceOutputTable = sourceOutputTable;
//		this.parentTable = parentTable;
//		
		
//		this.measureFields = new HashMap<VariableAggregate, Field<BigDecimal>>();
//		this.aggregateTables = new HashMap<AoiHierarchy, List<AggregateTable>>();
//		
		createPrimaryKeyField();
		createDimensionFieldsRecursive(entity);
		createStratumField();
		createAoiIdFields();
		createQuantityFields(false, true);
//		createMeasureFields(entity);
		createParentIdField();
		createAggregateTables();
	}

//	protected void createMeasureFields(Entity entity) {
//		List<VariableAggregate> aggregates = entity.getVariableAggregates();
//		for ( VariableAggregate agg : aggregates ) {
//			Field<BigDecimal> field = createField(agg.getName(), Psql.DOUBLE_PRECISION, this);
//			measureFields.put(agg, field);
//		}
//	}
//	
	protected void createAggregateTables() {
		Entity entity = getEntity();
	
		DataTable sourceTable = null;
		if( entity.getParent().isSamplingUnit() ){
			this.samplingUnitAggregateTable = new SamplingUnitAggregateTable(this);
			sourceTable = this.samplingUnitAggregateTable;
		}
		
		if( this.isGeoreferenced() ) {
			sourceTable = sourceTable == null ? this : sourceTable;
			this.aoiAggregateTables = new LinkedHashMap<AoiLevel, AoiAggregateTable>();
			createAoiAggregateTables( sourceTable );
		}
		
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
	}

	private void createAoiAggregateTables(DataTable sourceTable) {
		Workspace workspace = getEntity().getWorkspace();
		for ( AoiHierarchy aoiHierarchy : workspace.getAoiHierarchies() ) {
			
			for ( AoiLevel aoiLevel : aoiHierarchy.getLevels() ) {
				AoiAggregateTable aggTable = new AoiAggregateTable(sourceTable, aoiLevel);
				this.aoiAggregateTables.put(aoiLevel, aggTable);
			}
			
		}
	}

	public SamplingUnitAggregateTable getSamplingUnitAggregateTable() {
		return samplingUnitAggregateTable;
	}
	
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

	public Field<BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}
	
	public EntityDataView getEntityView() {
		return entityView;
	}

	public SamplingUnitAggregateTable getPlotAggregateTable() {
		if( getEntity().getParent().isSamplingUnit() ){
			return new SamplingUnitAggregateTable(this);
		}
		return null;
	}

	public Collection<AoiAggregateTable> getAoiAggregateTables() {
		return CollectionUtils.unmodifiableCollection( aoiAggregateTables.values() );
	}
	
	public InputSchema getDataSchema() {
		return schema;
	}

}
