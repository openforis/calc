/**
 * 
 */
package org.openforis.calc.schema;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.ErrorSettings;
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
	
	/**
	 * Map of variableId -> list of error tables
	 */
	private Map< Long, List<ErrorTable> > errorTables;
	
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
		createSamplingUnitIdField();
		createAggregateTables();
		createClusterField();
		
		createWeightField();
		
		createErrorTables();
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
	}

	protected void createAggregateTables() {
		this.aoiAggregateTables = new LinkedHashMap<AoiLevel, AoiAggregateTable>();

		DataTable sourceTable = null;
		Entity parent = getEntity().getParent();
		if ( parent != null && parent.isInSamplingUnitHierarchy() ) {
			this.samplingUnitAggregateTable = new SamplingUnitAggregateTable(this);
			sourceTable = this.samplingUnitAggregateTable;
		}

		if ( this.isGeoreferenced() ) {
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
	/**
	 * creates error tables
	 */
	private void createErrorTables() {
		this.errorTables = new HashMap< Long, List<ErrorTable> >();
		
		Workspace workspace = getWorkspace();
		ErrorSettings errorSettings = workspace.getErrorSettings();
		
		Collection<QuantitativeVariable> outputVariables = getEntity().getDefaultProcessingChainQuantitativeOutputVariables();
		for ( QuantitativeVariable quantitativeVariable : outputVariables ){
			long variableId = quantitativeVariable.getId().longValue();
			if( errorSettings.hasErrorSettings(variableId) ){
				
				Collection<? extends Number> aois 					= errorSettings.getAois( variableId );
				Collection<? extends Number> categoricalVariables 	= errorSettings.getCategoricalVariables( variableId );
				
				for ( Number aoiId : aois ){
					for ( Number categoricalVariableId : categoricalVariables ){
						CategoricalVariable<?> categoricalVariable = (CategoricalVariable<?>) workspace.getVariableById( categoricalVariableId.intValue() );
						Aoi aoi = workspace.getAoiHierarchies().get(0).getAoiById( aoiId.intValue() );
						
						ErrorTable errorTable = new ErrorTable( quantitativeVariable, aoi, categoricalVariable, schema );
						List<ErrorTable> variableErrorTables = getOrCreateErrorTables( variableId );
						variableErrorTables.add( errorTable );
					}
				}
			}
		}
		
	}

	private List<ErrorTable> getOrCreateErrorTables( long variableId ){
		List<ErrorTable> tables = null ;
		if( this.errorTables.containsKey(variableId) ){
			tables = this.errorTables.get( variableId );
		} else {
			tables = new ArrayList<ErrorTable>();
			this.errorTables.put( variableId , tables );
		}
		return tables;
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
	
	public List<ErrorTable> getErrorTables(){
		List<ErrorTable> list = new ArrayList<ErrorTable>();
		for ( List<ErrorTable> errorTables : this.errorTables.values() ){
			list.addAll( errorTables );
		}
		return CollectionUtils.unmodifiableList( list );
	}

	public List<ErrorTable> getErrorTables( QuantitativeVariable variable ) {
		List<ErrorTable> list = this.errorTables.get( variable.getId().longValue() );
		return CollectionUtils.unmodifiableList( list );
	}
	
}
