package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PlotAoiStratumAggregateTable extends AoiStratumAggregateTable<PlotFactTable> {

	private static final long serialVersionUID = 1L;
	
	PlotAoiStratumAggregateTable(PlotFactTable factTable, AoiHierarchyLevelMetadata aoiHierarchyLevel) {
		super(factTable, aoiHierarchyLevel);
	}

	@Override
	protected void initFields() {
		PlotFactTable fact = getFactTable();
		
		createFixedDimensionField(fact.STRATUM_ID);
		createFixedMeasureField(fact.EST_AREA);
		
		List<Field<Integer>> aoiFields = createAoiFields( getAoiHierarchyLevelMetadata().getAoiHierarchyLevelName());
		setAoiFields(aoiFields);
		
		initUserDefinedFields();
	}

}
