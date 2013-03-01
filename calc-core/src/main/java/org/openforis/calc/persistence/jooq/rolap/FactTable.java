/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import static org.jooq.impl.SQLDataType.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.calc.model.AoiHierarchyMetadata;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;

/**
 * @author M. Togna
 * @author G. Miceli
 * 
 */
public abstract class FactTable extends RolapTable {

	private static final long serialVersionUID = 1L;

	public final TableField<Record, BigDecimal> COUNT = createFixedMeasureField("cnt");
	
	private List<TableField<Record, Integer>> userDefinedDimensionFields;
	private List<TableField<Record, BigDecimal>> userDefinedMeasureFields;
	private List<TableField<Record, Integer>> fixedDimensionFields;
	private List<TableField<Record, BigDecimal>> fixedMeasureFields;
	
	private ObservationUnitMetadata observationUnitMetadata;
	
	FactTable(String schema, String name, ObservationUnitMetadata unit) {
		super(schema, name);
		observationUnitMetadata = unit;
	}

	/**
	 * Called by sub-classes to set up user-defined fields
	 */
	protected final void initUserDefinedFields() {
		this.userDefinedDimensionFields = new ArrayList<TableField<Record, Integer>>();
		this.userDefinedMeasureFields = new ArrayList<TableField<Record, BigDecimal>>();
		createUserDefinedFields(observationUnitMetadata);
	}
	
	private void createUserDefinedFields(ObservationUnitMetadata unit) {
		if ( unit.getObsUnitParent() != null ) {
			createUserDefinedFields( unit.getObsUnitParent() );
		}
		Collection<VariableMetadata> vars = unit.getVariableMetadata();
		for (VariableMetadata var : vars) {
			if ( var.isForAnalysis() ) {
				String name = var.getVariableName();
				if ( var.isCategorical() ) {
					createUserDefinedDimensionField(name);
				} else if ( var.isNumeric() ) {
					createUserDefinedMeasureField(name);					
				} 
			}
		}
	}
	
	public ObservationUnitMetadata getObservationUnitMetadata() {
		return observationUnitMetadata;
	}

	void createUserDefinedDimensionField(String name) {
		if ( getField(name) != null ) {
			throw new IllegalArgumentException("Field '"+name+"' already exists!");
		}
		TableField<Record, Integer> field = createField(name, INTEGER);
		userDefinedDimensionFields.add(field);
	}

	void createUserDefinedMeasureField(String name) {
		if ( getField(name) != null ) {
			throw new IllegalArgumentException("Field '"+name+"' already exists!");
		}
		TableField<Record, BigDecimal> field = createField(name, NUMERIC);
		userDefinedMeasureFields.add(field);
	}

	public List<TableField<Record, Integer>> getUserDefinedDimensionFields() {
		return Collections.unmodifiableList(userDefinedDimensionFields);
	}

	public List<TableField<Record, BigDecimal>> getUserDefinedMeasureFields() {
		return Collections.unmodifiableList(userDefinedMeasureFields);
	}

	public List<TableField<Record, Integer>> getFixedDimensionFields() {
		return Collections.unmodifiableList(fixedDimensionFields);
	}

	public List<TableField<Record, BigDecimal>> getFixedMeasureFields() {
		return Collections.unmodifiableList(fixedMeasureFields);
	}
	
	protected TableField<Record, BigDecimal> createFixedMeasureField(String name) {
		if ( fixedMeasureFields == null ) {
			fixedMeasureFields = new ArrayList<TableField<Record,BigDecimal>>();
		}
		TableField<Record, BigDecimal> fld = super.createField(name, NUMERIC);
		fixedMeasureFields.add(fld);
		return fld;
	}
	
	protected TableField<Record, BigDecimal> createFixedMeasureField(TableField<Record, BigDecimal> fld) {
		return createFixedMeasureField(fld.getName());
	}
	
	protected TableField<Record, Integer> createFixedDimensionField(String name) {
		if ( fixedDimensionFields == null ) {
			fixedDimensionFields = new ArrayList<TableField<Record,Integer>>();
		}
		TableField<Record, Integer> fld = super.createField(name, INTEGER);
		fixedDimensionFields.add(fld);
		return fld;
	}
	
	protected TableField<Record, Integer> createFixedDimensionField(TableField<Record, Integer> fld) {
		return createFixedDimensionField(fld.getName());
	}

	protected List<TableField<Record, Integer>> createAoiFields() {
		return createAoiFields(null);
	}
	
	protected List<TableField<Record, Integer>> createAoiFields(String aoiLevel) {
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
		// TODO multiple hierarchies
		AoiHierarchyMetadata hier = aoiHierarchies.get(0);
		List<AoiHierarchyLevelMetadata> levels = hier.getLevelMetadata();
		List<TableField<Record,Integer>> aoiFields = new ArrayList<TableField<Record,Integer>>();
		for (AoiHierarchyLevelMetadata level : levels) {
			String name = level.getAoiHierarchyLevelName();
			TableField<Record, Integer> fld = createField(name, INTEGER);
			aoiFields.add(fld);
			if ( name.equals(aoiLevel) ) {
				break;
			}
		}
		return aoiFields;
	}

}
