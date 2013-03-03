package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.Tables.*;

import org.jooq.Field;
import org.openforis.calc.model.ObservationUnitMetadata;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class InterviewFactTable extends FactTable {
	private static final long serialVersionUID = 1L;
	
	// Fixed dimensions
	public final Field<Integer> CLUSTER_ID = createFixedDimensionField(INTERVIEW.CLUSTER_ID);
	
	// Fixed measures
//	public final Field<BigDecimal> PLOT_LOCATION_DEVIATION = 
//			createFixedMeasureField(G.PLOT_LOCATION_DEVIATION.getName());

	// Plot coordinates
	public final Field<Object> PLOT_LOCATION = createField(INTERVIEW.INTERVIEW_LOCATION);

	InterviewFactTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getFactTableName(), unit);
		initFields();
	}

	protected void initFields() {
		initUserDefinedFields();
	}
}
