/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.BOOLEAN;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.SQLDataType;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.AoiHierarchy;
import org.openforis.calc.metadata.AoiHierarchyLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.psql.GeodeticCoordinate;
import org.openforis.calc.psql.Psql;

/**
 * A table derived from a Calc entity; this includes input and output data tables as well as OLAP fact tables
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class DataTable extends AbstractTable {

	private static final long serialVersionUID = 1L;

	private static final String LOCATION_COLUMN_NAME = "_location";
	
	private Entity entity;

	private UniqueKey<Record> primaryKey;

	private Map<AoiHierarchyLevel, Field<Integer>> aoiIdFields;
	private Map<QuantitativeVariable, Field<BigDecimal>> quantityFields;
	private Map<CategoricalVariable, Field<?>> categoryValueFields;
	
	private TableField<Record, Integer> idField;
	private Field<Integer> stratumIdField;
	private Field<GeodeticCoordinate> locationField;
	private Field<BigDecimal> xField;
	private Field<BigDecimal> yField;
	private Field<String> srsIdField;
	private Field<Integer> parentIdField;
	
	private DataTable sourceTable;
	private DataTable parentTable;
	
	protected DataTable(Entity entity, String name, Schema schema, DataTable sourceTable, DataTable parentTable) {
		super(name, schema);
		this.entity = entity;
		this.sourceTable = sourceTable;
		this.parentTable = parentTable;
	}

	@SuppressWarnings("unchecked")
	protected void createPrimaryKeyField() {
		this.idField = createField(entity.getIdColumn(), INTEGER, this);
		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
	}

	protected void createStratumIdField() {
		this.stratumIdField = createField("_stratum_id", INTEGER, this);
	}

	public Field<Integer> getStratumIdField() {
		return stratumIdField;
	}

	protected void createQuantityFields(boolean inputOnly) {
		this.quantityFields = new HashMap<QuantitativeVariable, Field<BigDecimal>>();
		Entity entity = getEntity();
		List<Variable> variables = entity.getVariables();
		for ( Variable var : variables ) {
			if ( var.isInput() || !inputOnly ) {
				if ( var instanceof QuantitativeVariable ) {
					createQuantityField((QuantitativeVariable) var);
				}
			}
		}
	}

	private void createQuantityField(QuantitativeVariable var) {
		Field<BigDecimal> field = createValueField(var, Psql.DOUBLE_PRECISION);
		quantityFields.put(var, field);
	}

	protected void createCategoryValueFields(Entity entity, boolean inputOnly, boolean disaggregatesOnly) {
		this.categoryValueFields = new HashMap<CategoricalVariable, Field<?>>();
		List<CategoricalVariable> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable var : variables ) {
			if ( (var.isInput() || !inputOnly) && (var.isDisaggregate() || !disaggregatesOnly) ) {
				if ( var instanceof BinaryVariable ) {
					Field<Boolean> fld = createValueField((BinaryVariable) var, BOOLEAN);
					categoryValueFields.put((CategoricalVariable) var, fld);
				} else {
					Field<String> fld = createValueField(var, VARCHAR.length(255));
					categoryValueFields.put((CategoricalVariable) var, fld);
				}
			}
		}
	}

	private <T> Field<T> createValueField(Variable var, DataType<T> valueType) {
		String valueColumn = var.getValueColumn();
		if ( valueColumn != null ) {
			return createField(valueColumn, valueType, this);
		}
		return null;				
	}

	public TableField<Record, Integer> getIdField() {
		return idField;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}

	public Field<Integer> getAoiIdField(AoiHierarchyLevel level) {
		return aoiIdFields == null ? null : aoiIdFields.get(level);
	}

	public Field<GeodeticCoordinate> getLocationField() {
		return locationField;
	}

	public Field<BigDecimal> getxField() {
		return xField;
	}
	
	public Field<BigDecimal> getyField() {
		return yField;
	}
	
	public Field<String> getSrsIdField() {
		return srsIdField;
	}
	
	protected void createAoiIdFields(AoiHierarchyLevel lowestLevel) {
		this.aoiIdFields = new HashMap<AoiHierarchyLevel, Field<Integer>>();
		if ( isGeoreferenced() ) {
			Workspace workspace = entity.getWorkspace();
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			for ( AoiHierarchy hierarchy : aoiHierarchies ) {
				List<AoiHierarchyLevel> levels = hierarchy.getLevels();
				for ( AoiHierarchyLevel level : levels ) {
					if ( lowestLevel == null || level.getRank() <= lowestLevel.getRank() ) {
						String fkColumn = level.getFkColumn();
						Field<Integer> aoiField = createField(fkColumn, INTEGER, this);
						aoiIdFields.put(level, aoiField);
					}
				}
			}
		}
	}

	protected void createLocationField() {
		if ( isGeoreferenced() ) {
			locationField = createField(LOCATION_COLUMN_NAME, Psql.GEODETIC_COORDINATE, this);
		}
	}

	protected void createCoordinateFields() {
		if ( isGeoreferenced() ) {
			String xColumn = entity.getXColumn();
			String yColumn = entity.getYColumn();
			String srsColumn = entity.getSrsColumn();

			if ( !(StringUtils.isBlank(xColumn) || StringUtils.isBlank(yColumn) || StringUtils.isBlank(srsColumn)) ) {
				xField = createField(xColumn, Psql.DOUBLE_PRECISION, this);
				yField = createField(yColumn, Psql.DOUBLE_PRECISION, this);
				srsIdField = createField(srsColumn, SQLDataType.VARCHAR, this);
			}
		}
	}

	protected void createParentIdField() {
		String parentIdColumn = entity.getParentIdColumn();
		if ( parentIdColumn == null ) {
			if ( entity.getParent() != null ) {
				throw new NullPointerException("parent_id_column not defined for entity "+entity);
			}
		} else {
			this.parentIdField = createField(parentIdColumn, INTEGER, this);
		}
	}
	
	public DataTable getSourceTable() {
		return sourceTable;
	}
	
	public Field<Integer> getParentIdField() {
		return parentIdField;
	}
	
	public DataTable getParentTable() {
		return parentTable;
	}
	
	public Collection<Field<?>> getCategoryValueFields() {
		return Collections.unmodifiableCollection(categoryValueFields.values());
	}
	
	public Collection<Field<Integer>> getAoiIdFields() {
		return Collections.unmodifiableCollection(aoiIdFields.values());
	}
	
	public Field<BigDecimal> getQuantityField(QuantitativeVariable var) {
		return quantityFields == null ? null : quantityFields.get(var);
	}
	
	public Field<?> getCategoryValueField(CategoricalVariable var) {
		return categoryValueFields.get(var);
	}
	
	// TODO HARD-CODED: allow user-configuration of variable to use as weight! 
	@SuppressWarnings("unchecked")
	public Field<BigDecimal> getWeightField() {
		return (Field<BigDecimal>) field("weight");
	}

	public boolean isGeoreferenced() {
		return getEntity().isGeoreferenced();
	}
}


