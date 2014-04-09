/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.psql.Psql;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author Mino Togna
 */
public class FactTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_fact";
	// private static final String DIMENSION_ID_COLUMN_FORMAT = "%s_id";

	private Map<AoiLevel, AoiAggregateTable> aoiAggregateTables;
	private SamplingUnitAggregateTable samplingUnitAggregateTable;

	private EntityDataView entityView;
	private Field<BigDecimal> plotAreaField;
	private DataSchema schema;
	private Map<QuantitativeVariable, Field<BigDecimal>> measureFields;
	private Field<String> clusterField; 
	
	FactTable(Entity entity, DataSchema schema) {
		this(entity, getName(entity), schema);
	}

	FactTable(Entity entity, String name, DataSchema schema) {
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

		this.measureFields = new HashMap<QuantitativeVariable, Field<BigDecimal>>();

		createPrimaryKeyField();
		createDimensionFieldsRecursive(entity);
		createStratumField();
		createAoiIdFields();
		createQuantityFields(false, true);
		createMeasureFields();
		createParentIdField();
		createAggregateTables();
		createClusterField();
		
		createWeightField();
	}

	private void createClusterField() {
		this.clusterField = createField( "_cluster", SQLDataType.VARCHAR, this );
		
	}

	protected void createMeasureFields() {
		Entity entity = getEntity();
		List<QuantitativeVariable> variables = entity.getQuantitativeVariables();
		for (QuantitativeVariable var : variables) {
			Field<BigDecimal> field = createField(var.getName(), Psql.DOUBLE_PRECISION, this);
			measureFields.put(var, field);
		}

		// List<VariableAggregate> aggregates = entity.getVariableAggregates();
		// for ( VariableAggregate agg : aggregates ) {
		// }
	}

	protected void createAggregateTables() {
		this.aoiAggregateTables = new LinkedHashMap<AoiLevel, AoiAggregateTable>();
		Entity entity = getEntity();

		DataTable sourceTable = null;
		if (entity.getParent().isSamplingUnit()) {
			this.samplingUnitAggregateTable = new SamplingUnitAggregateTable(this);
			sourceTable = this.samplingUnitAggregateTable;
		}

		if (this.isGeoreferenced()) {
			sourceTable = sourceTable == null ? this : sourceTable;
			createAoiAggregateTables(sourceTable);
		}
	}

	private void createAoiAggregateTables(DataTable sourceTable) {
		Workspace workspace = getEntity().getWorkspace();
		for (AoiHierarchy aoiHierarchy : workspace.getAoiHierarchies()) {

			for (AoiLevel aoiLevel : aoiHierarchy.getLevels()) {
				AoiAggregateTable aggTable = new AoiAggregateTable(sourceTable, aoiLevel);
				this.aoiAggregateTables.put(aoiLevel, aggTable);
			}

		}
	}

	public SamplingUnitAggregateTable getSamplingUnitAggregateTable() {
		return samplingUnitAggregateTable;
	}

	private static String getName(Entity entity) {
		return String.format(TABLE_NAME_FORMAT, entity.getDataTable());
	}

	public Field<BigDecimal> getMeasureField(QuantitativeVariable variable) {
		return measureFields.get(variable);
	}

	public Field<BigDecimal> getPlotAreaField() {
		return plotAreaField;
	}

	public Field<String> getClusterField() {
		return clusterField;
	}
	
	public EntityDataView getEntityView() {
		return entityView;
	}

	public SamplingUnitAggregateTable getPlotAggregateTable() {
		if (getEntity().getParent().isSamplingUnit()) {
			return new SamplingUnitAggregateTable(this);
		}
		return null;
	}

	public Collection<AoiAggregateTable> getAoiAggregateTables() {
		return CollectionUtils.unmodifiableCollection(aoiAggregateTables.values());
	}

	public DataSchema getDataSchema() {
		return schema;
	}
	

}
