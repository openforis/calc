package org.openforis.calc.persistence.jooq.rolap;

import java.util.Collections;
import java.util.List;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class SpecimenFactTable extends FactTable {
	private static final long serialVersionUID = 1L;

	private List<TableField<Record, Integer>> aoiFields;

	SpecimenFactTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getFactTableName(), unit);
		initFields();
	}

	protected void initFields() {
		// TODO 
//		aoiFields = createAoiFields();
		initUserDefinedFields();
	}
	
	public List<TableField<Record, Integer>> getAoiFields() {
		return Collections.unmodifiableList(aoiFields);
	}
}
