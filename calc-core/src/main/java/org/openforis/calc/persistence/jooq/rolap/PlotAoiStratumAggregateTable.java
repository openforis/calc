package org.openforis.calc.persistence.jooq.rolap;

import java.util.Collections;
import java.util.List;

import org.jooq.Field;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PlotAoiStratumAggregateTable extends AggregateTable<PlotFactTable> {

	private static final long serialVersionUID = 1L;
	
	private static final String INFIX_SUFFIX = "_stratum";

	private List<Field<Integer>> aoiFields;

	private String aoiLevel;

	PlotAoiStratumAggregateTable(PlotFactTable factTable, String aoiLevel) {
		super(factTable, getInfix(aoiLevel));
		this.aoiLevel = aoiLevel;
		initFields();
	}

	private static String getInfix(String aoiLevel) {
		return aoiLevel + INFIX_SUFFIX;
	}
	
	protected void initFields() {
		PlotFactTable fact = getFactTable();
		createFixedDimensionField(fact.STRATUM_ID);
		createFixedMeasureField(fact.EST_AREA);
		aoiFields = createAoiFields(aoiLevel);
		initUserDefinedFields();
	}

	public List<Field<Integer>> getAoiFields() {
		return Collections.unmodifiableList(aoiFields);
	}
	
	public String getAoiLevel() {
		return aoiLevel;
	}
}
