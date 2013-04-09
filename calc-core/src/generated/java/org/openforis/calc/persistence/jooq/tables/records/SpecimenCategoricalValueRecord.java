/**
 * This class is generated by jOOQ
 */
package org.openforis.calc.persistence.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.6.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings("all")
public class SpecimenCategoricalValueRecord extends org.jooq.impl.UpdatableRecordImpl<org.openforis.calc.persistence.jooq.tables.records.SpecimenCategoricalValueRecord> {

	private static final long serialVersionUID = 1525174206;

	/**
	 * The table column <code>calc.specimen_categorical_value.value_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public void setValueId(java.lang.Integer value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.VALUE_ID, value);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.value_id</code>
	 * <p>
	 * This column is part of the table's PRIMARY KEY
	 */
	public java.lang.Integer getValueId() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.VALUE_ID);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.specimen_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT specimen_categorical_value__specimen_categorical_value_specimen_fkey
	 * FOREIGN KEY (specimen_id)
	 * REFERENCES calc.specimen (specimen_id)
	 * </pre></code>
	 */
	public void setSpecimenId(java.lang.Integer value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.SPECIMEN_ID, value);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.specimen_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT specimen_categorical_value__specimen_categorical_value_specimen_fkey
	 * FOREIGN KEY (specimen_id)
	 * REFERENCES calc.specimen (specimen_id)
	 * </pre></code>
	 */
	public java.lang.Integer getSpecimenId() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.SPECIMEN_ID);
	}

	/**
	 * Link this record to a given {@link org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord 
	 * SpecimenRecord}
	 */
	public void setSpecimenId(org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord value) {
		if (value == null) {
			setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.SPECIMEN_ID, null);
		}
		else {
			setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.SPECIMEN_ID, value.getValue(org.openforis.calc.persistence.jooq.tables.Specimen.SPECIMEN.SPECIMEN_ID));
		}
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.specimen_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT specimen_categorical_value__specimen_categorical_value_specimen_fkey
	 * FOREIGN KEY (specimen_id)
	 * REFERENCES calc.specimen (specimen_id)
	 * </pre></code>
	 */
	public org.openforis.calc.persistence.jooq.tables.records.SpecimenRecord fetchSpecimen() {
		return create()
			.selectFrom(org.openforis.calc.persistence.jooq.tables.Specimen.SPECIMEN)
			.where(org.openforis.calc.persistence.jooq.tables.Specimen.SPECIMEN.SPECIMEN_ID.equal(getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.SPECIMEN_ID)))
			.fetchOne();
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.category_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT specimen_categorical_value__specimen_categorical_value_category_fkey
	 * FOREIGN KEY (category_id)
	 * REFERENCES calc.category (category_id)
	 * </pre></code>
	 */
	public void setCategoryId(java.lang.Integer value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CATEGORY_ID, value);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.category_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT specimen_categorical_value__specimen_categorical_value_category_fkey
	 * FOREIGN KEY (category_id)
	 * REFERENCES calc.category (category_id)
	 * </pre></code>
	 */
	public java.lang.Integer getCategoryId() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CATEGORY_ID);
	}

	/**
	 * Link this record to a given {@link org.openforis.calc.persistence.jooq.tables.records.CategoryRecord 
	 * CategoryRecord}
	 */
	public void setCategoryId(org.openforis.calc.persistence.jooq.tables.records.CategoryRecord value) {
		if (value == null) {
			setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CATEGORY_ID, null);
		}
		else {
			setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CATEGORY_ID, value.getValue(org.openforis.calc.persistence.jooq.tables.Category.CATEGORY.CATEGORY_ID));
		}
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.category_id</code>
	 * <p>
	 * This column is part of a FOREIGN KEY: <code><pre>
	 * CONSTRAINT specimen_categorical_value__specimen_categorical_value_category_fkey
	 * FOREIGN KEY (category_id)
	 * REFERENCES calc.category (category_id)
	 * </pre></code>
	 */
	public org.openforis.calc.persistence.jooq.tables.records.CategoryRecord fetchCategory() {
		return create()
			.selectFrom(org.openforis.calc.persistence.jooq.tables.Category.CATEGORY)
			.where(org.openforis.calc.persistence.jooq.tables.Category.CATEGORY.CATEGORY_ID.equal(getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CATEGORY_ID)))
			.fetchOne();
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.original</code>
	 */
	public void setOriginal(java.lang.Boolean value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.ORIGINAL, value);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.original</code>
	 */
	public java.lang.Boolean getOriginal() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.ORIGINAL);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.current</code>
	 */
	public void setCurrent(java.lang.Boolean value) {
		setValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CURRENT, value);
	}

	/**
	 * The table column <code>calc.specimen_categorical_value.current</code>
	 */
	public java.lang.Boolean getCurrent() {
		return getValue(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE.CURRENT);
	}

	/**
	 * Create a detached SpecimenCategoricalValueRecord
	 */
	public SpecimenCategoricalValueRecord() {
		super(org.openforis.calc.persistence.jooq.tables.SpecimenCategoricalValue.SPECIMEN_CATEGORICAL_VALUE);
	}
}