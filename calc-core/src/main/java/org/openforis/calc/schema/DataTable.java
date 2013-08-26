/**
 * 
 */
package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.BOOLEAN;
import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;

import java.math.BigDecimal;
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
import org.openforis.calc.persistence.postgis.GeodeticCoordinate;
import org.openforis.calc.persistence.postgis.Psql;

/**
 * A table derived from a Calc entity; this includes input and output data tables as well as OLAP fact tables
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public abstract class DataTable extends AbstractTable {

	private static final String LOCATION_COLUMN_NAME = "_location";
	private static final long serialVersionUID = 1L;

	private final TableField<Record, Integer> idField;

	private Entity entity;

	private UniqueKey<Record> primaryKey;

	private Map<AoiHierarchyLevel, Field<Integer>> aoiIdFields;

	private Field<Integer> stratumIdField;
	private Field<GeodeticCoordinate> locationField;
	private Field<BigDecimal> xField;
	private Field<BigDecimal> yField;
	private Field<String> srsIdField;

	private DataTable sourceTable;

	@SuppressWarnings("unchecked")
	protected DataTable(Entity entity, String name, Schema schema, DataTable sourceTable) {
		super(name, schema);
		this.entity = entity;
		this.idField = createField(entity.getIdColumn(), INTEGER, this);
		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
		this.sourceTable = sourceTable;
	}

	protected void createStratumIdField() {
		this.stratumIdField = createField("_stratum_id", INTEGER, this);
	}

	public Field<Integer> getStratumIdField() {
		return stratumIdField;
	}

	protected void createQuantityFields(boolean inputOnly) {
		Entity entity = getEntity();
		List<Variable> variables = entity.getVariables();
		for ( Variable var : variables ) {
			if ( var.isInput() || !inputOnly ) {
				if ( var instanceof QuantitativeVariable ) {
					createValueField((QuantitativeVariable) var, Psql.DOUBLE_PRECISION);
				}
			}
		}
	}

	protected void createCategoryFields(Entity entity, boolean inputOnly) {
		List<Variable> variables = entity.getVariables();
		for ( Variable var : variables ) {
			if ( var.isInput() || !inputOnly ) {
				if ( var instanceof BinaryVariable ) {
					createValueField((BinaryVariable) var, BOOLEAN);
					createCategoryIdField((BinaryVariable) var);
				} else if ( var instanceof CategoricalVariable ) {
					createValueField(var, VARCHAR.length(255));
					createCategoryIdField((CategoricalVariable) var);
				}
			}
		}
	}

	protected void createCategoryIdField(CategoricalVariable var) {
		String categoryIdColumn = var.getCategoryIdColumn();
		if ( categoryIdColumn != null ) {
			createField(categoryIdColumn, INTEGER, this);
		}
	}

	private void createValueField(Variable var, DataType<?> valueType) {
		String valueColumn = var.getValueColumn();
		if ( valueColumn != null ) {
			createField(valueColumn, valueType, this);
		}
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
	
	protected void createAoiIdFields() {
		this.aoiIdFields = new HashMap<AoiHierarchyLevel, Field<Integer>>();
		if ( entity.isGeoreferenced() ) {
			Workspace workspace = entity.getWorkspace();
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			for ( AoiHierarchy hierarchy : aoiHierarchies ) {
				List<AoiHierarchyLevel> levels = hierarchy.getLevels();
				for ( AoiHierarchyLevel level : levels ) {
					String fkColumn = level.getFkColumn();
					Field<Integer> aoiField = createField(fkColumn, INTEGER, this);
					aoiIdFields.put(level, aoiField);
				}
			}
		}
	}

	protected void createLocationField() {
		if ( entity.isGeoreferenced() ) {
			locationField = createField(LOCATION_COLUMN_NAME, Psql.GEODETIC_COORDINATE, this);
		}
	}

	protected void createCoordinateFields() {
		if ( entity.isGeoreferenced() ) {
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

	public DataTable getSourceTable() {
		return sourceTable;
	}
}
