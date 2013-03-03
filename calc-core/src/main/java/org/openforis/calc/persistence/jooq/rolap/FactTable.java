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

import org.jooq.Field;
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

	public final Field<BigDecimal> COUNT = createFixedMeasureField("cnt");
	
	private List<Field<Integer>> userDefinedDimensionFields;
	private List<Field<BigDecimal>> userDefinedMeasureFields;
	private List<Field<Integer>> fixedDimensionFields;
	private List<Field<BigDecimal>> fixedMeasureFields;
	
	private ObservationUnitMetadata observationUnitMetadata;
	
	FactTable(String schema, String name, ObservationUnitMetadata unit) {
		super(schema, name);
		observationUnitMetadata = unit;
	}

	/**
	 * Called by sub-classes to set up user-defined fields
	 */
	protected final void initUserDefinedFields() {
		this.userDefinedDimensionFields = new ArrayList<Field<Integer>>();
		this.userDefinedMeasureFields = new ArrayList<Field<BigDecimal>>();
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
		Field<Integer> field = createField(name, INTEGER);
		userDefinedDimensionFields.add(field);
	}

	void createUserDefinedMeasureField(String name) {
		if ( getField(name) != null ) {
			throw new IllegalArgumentException("Field '"+name+"' already exists!");
		}
		Field<BigDecimal> field = createField(name, NUMERIC);
		userDefinedMeasureFields.add(field);
	}

	public List<Field<Integer>> getUserDefinedDimensionFields() {
		return Collections.unmodifiableList(userDefinedDimensionFields);
	}

	public List<Field<BigDecimal>> getUserDefinedMeasureFields() {
		return Collections.unmodifiableList(userDefinedMeasureFields);
	}

	public List<Field<Integer>> getFixedDimensionFields() {
		return Collections.unmodifiableList(fixedDimensionFields);
	}

	public List<Field<BigDecimal>> getFixedMeasureFields() {
		return Collections.unmodifiableList(fixedMeasureFields);
	}
	
	protected Field<BigDecimal> createFixedMeasureField(String name) {
		if ( fixedMeasureFields == null ) {
			fixedMeasureFields = new ArrayList<Field<BigDecimal>>();
		}
		Field<BigDecimal> fld = super.createField(name, NUMERIC);
		fixedMeasureFields.add(fld);
		return fld;
	}
	
	protected Field<BigDecimal> createFixedMeasureField(Field<? extends Number> fld) {
		return createFixedMeasureField(fld.getName());
	}
	
	protected Field<Integer> createFixedDimensionField(String name) {
		if ( fixedDimensionFields == null ) {
			fixedDimensionFields = new ArrayList<Field<Integer>>();
		}
		Field<Integer> fld = super.createField(name, INTEGER);
		fixedDimensionFields.add(fld);
		return fld;
	}
	
	protected Field<Integer> createFixedDimensionField(Field<Integer> fld) {
		return createFixedDimensionField(fld.getName());
	}

	protected List<Field<Integer>> createAoiFields() {
		return createAoiFields(null);
	}
	
	protected List<Field<Integer>> createAoiFields(String aoiLevel) {
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		SurveyMetadata survey = unit.getSurveyMetadata();
		List<AoiHierarchyMetadata> aoiHierarchies = survey.getAoiHierarchyMetadata();
		// TODO multiple hierarchies
		AoiHierarchyMetadata hier = aoiHierarchies.get(0);
		List<AoiHierarchyLevelMetadata> levels = hier.getLevelMetadata();
		List<Field<Integer>> aoiFields = new ArrayList<Field<Integer>>();
		for (AoiHierarchyLevelMetadata level : levels) {
			String name = level.getAoiHierarchyLevelName();
			Field<Integer> fld = createField(name, INTEGER);
			aoiFields.add(fld);
			if ( name.equals(aoiLevel) ) {
				break;
			}
		}
		return aoiFields;
	}

}
