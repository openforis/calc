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
import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.BinaryVariable;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
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

	private Entity entity;
	private UniqueKey<Record> primaryKey;
	
	private Map<AoiLevel, Field<Integer>> aoiIdFields;
	private Map<QuantitativeVariable, Field<BigDecimal>> quantityFields;
	private Map<CategoricalVariable<?>, Field<?>> categoryValueFields;
	
	private TableField<Record, Integer> idField;
	private Field<GeodeticCoordinate> locationField;
	private Field<BigDecimal> xField;
	private Field<BigDecimal> yField;
	private Field<String> srsIdField;
	private Field<Integer> parentIdField;
	
	protected DataTable(Entity entity, String name, Schema schema) {
		super(name, schema);
		this.entity = entity;
		this.aoiIdFields = new HashMap<AoiLevel, Field<Integer>>();
		this.quantityFields = new HashMap<QuantitativeVariable, Field<BigDecimal>>();
		this.categoryValueFields = new HashMap<CategoricalVariable<?>, Field<?>>();
	}

	@SuppressWarnings("unchecked")
	protected void createPrimaryKeyField() {
		this.idField = createField(entity.getIdColumn(), INTEGER, this);
		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
	}

	protected void createQuantityField(QuantitativeVariable var, String valueColumn) {
		Field<BigDecimal> field = createValueField(var, Psql.DOUBLE_PRECISION, valueColumn);
		quantityFields.put(var, field);
	}
	
	protected void createQuantityFields(boolean input) {
		Entity entity = getEntity();
		List<Variable<?>> variables = entity.getVariables();
		for ( Variable<?> var : variables ) {
			if ( var instanceof QuantitativeVariable ) {
				String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
				if ( valueColumn != null ) {
					createQuantityField((QuantitativeVariable) var, valueColumn);
				}
			}
		}
	}

	protected void createCategoryValueFields(Entity entity, boolean input) {
		List<CategoricalVariable<?>> variables = entity.getCategoricalVariables();
		for ( CategoricalVariable<?> var : variables ) {
			String valueColumn = input ? var.getInputValueColumn() : var.getOutputValueColumn();
			if ( valueColumn != null ) {
				if ( var instanceof BinaryVariable ) {
					createBinaryCategoryValueField((BinaryVariable) var, valueColumn);
				} else if ( var instanceof MultiwayVariable ) {
					createCategoryValueField((MultiwayVariable) var, valueColumn);
				}
			}
		}
	}

	protected void createCategoryValueField(MultiwayVariable var, String valueColumn) {
		Field<String> fld = createValueField(var, VARCHAR.length(255), valueColumn);
		categoryValueFields.put(var, fld);
	}

	protected void createBinaryCategoryValueField(BinaryVariable var, String valueColumn) {
		Field<Boolean> fld = createValueField((BinaryVariable) var, BOOLEAN, valueColumn);
		categoryValueFields.put(var, fld);
	}

	private <T> Field<T> createValueField(Variable<?> var, DataType<T> valueType, String valueColumn) {
		if ( valueColumn == null ) {
			return null;
		} else {
			return createField(valueColumn, valueType, this);
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

	public Field<Integer> getAoiIdField(AoiLevel level) {
		return aoiIdFields == null ? null : aoiIdFields.get(level);
	}

	public Field<GeodeticCoordinate> getLocationField() {
		return locationField;
	}

	public Field<BigDecimal> getXField() {
		return xField;
	}
	
	public Field<BigDecimal> getYField() {
		return yField;
	}
	
	public Field<String> getSrsIdField() {
		return srsIdField;
	}
	
	protected void createAoiIdFields() {
		createAoiIdFields(null);
	}
	
	protected void createAoiIdFields(AoiLevel lowestLevel) {
		if ( isGeoreferenced() ) {
			Workspace workspace = entity.getWorkspace();
			List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
			for ( AoiHierarchy hierarchy : aoiHierarchies ) {
				List<AoiLevel> levels = hierarchy.getLevels();
				for ( AoiLevel level : levels ) {
					if ( lowestLevel == null || level.getRank() <= lowestLevel.getRank() ) {
						String fkColumn = level.getFkColumn();
						createAoiIdField(level, fkColumn);
					}
				}
			}
		}
	}

	protected void createAoiIdField(AoiLevel level, String fkColumn) {
		Field<Integer> aoiField = createField(fkColumn, INTEGER, this);
		aoiIdFields.put(level, aoiField);
	}

	protected void createLocationField() {
		if ( isGeoreferenced() ) {
			locationField = createField("_location", Psql.GEODETIC_COORDINATE, this);
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
	
	public Field<Integer> getParentIdField() {
		return parentIdField;
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
	
	public Field<?> getCategoryValueField(CategoricalVariable<?> var) {
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


