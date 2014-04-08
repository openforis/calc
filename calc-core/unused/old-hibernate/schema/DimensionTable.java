package org.openforis.calc.schema;

import static org.jooq.impl.SQLDataType.INTEGER;
import static org.jooq.impl.SQLDataType.VARCHAR;
import static org.openforis.calc.psql.Psql.DOUBLE_PRECISION;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public class DimensionTable extends AbstractTable {

	private static final long serialVersionUID = 1L;

	private TableField<Record, Integer> idField;
	private TableField<Record, String> captionField;

	private TableField<Record, String> codeField;
	private TableField<Record, String> descriptionField;
	private TableField<Record, Integer> sortOrderField;
	private TableField<Record, BigDecimal> valueField;

	private UniqueKey<Record> primaryKey;

	DimensionTable(String name, RelationalSchema schema) {
		super(name, schema);

		// initFields();
	}

	@SuppressWarnings("unchecked")
	protected void initFields() {
		idField = createField("id", INTEGER, this);
		codeField = createField("code", VARCHAR.length(25), this);
		captionField = createField("caption", VARCHAR.length(255), this);
		descriptionField = createField("description", VARCHAR.length(1024), this);
		sortOrderField = createField("sort_order", INTEGER, this);
		valueField = createField("value", DOUBLE_PRECISION, this);

		this.primaryKey = KeyFactory.newUniqueKey(this, idField);
	}

	@Override
	public UniqueKey<Record> getPrimaryKey() {
		return primaryKey;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public TableField<Record, Integer> getIdField() {
		return idField;
	}

	public TableField<Record, String> getCaptionField() {
		return captionField;
	}

	public TableField<Record, String> getCodeField() {
		return codeField;
	}

	public TableField<Record, String> getDescriptionField() {
		return descriptionField;
	}

	public TableField<Record, Integer> getSortOrderField() {
		return sortOrderField;
	}

	public TableField<Record, BigDecimal> getValueField() {
		return valueField;
	}

	protected void setIdField(TableField<Record, Integer> idField) {
		this.idField = idField;
	}

	protected void setCaptionField(TableField<Record, String> captionField) {
		this.captionField = captionField;
	}

	protected void setCodeField(TableField<Record, String> codeField) {
		this.codeField = codeField;
	}

	protected void setDescriptionField(TableField<Record, String> descriptionField) {
		this.descriptionField = descriptionField;
	}

	protected void setSortOrderField(TableField<Record, Integer> sortOrderField) {
		this.sortOrderField = sortOrderField;
	}

	protected void setValueField(TableField<Record, BigDecimal> valueField) {
		this.valueField = valueField;
	}

	protected void setPrimaryKey(UniqueKey<Record> primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	
}
