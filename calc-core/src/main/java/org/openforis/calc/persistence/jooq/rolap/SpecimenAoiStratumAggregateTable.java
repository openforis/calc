package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;

/**
 * 
 * @author M. Togna
 *
 */
public class SpecimenAoiStratumAggregateTable extends AoiStratumAggregateTable<SpecimenPlotAggregateTable> {

	private static final long serialVersionUID = 1L;
	

	SpecimenAoiStratumAggregateTable(SpecimenPlotAggregateTable factTable, AoiHierarchyLevelMetadata aoiHierarchyLevel) {
		super(factTable, aoiHierarchyLevel);
	}

	@Override
	protected void initFields() {
		SpecimenPlotAggregateTable fact = getFactTable();
		SpecimenFactTable specimenFact = fact.getFactTable();
		
		createFixedDimensionField(specimenFact.STRATUM_ID);
		createFixedDimensionField(specimenFact.SPECIMEN_TAXON_ID);
		
		List<Field<Integer>> aoiFields = createAoiFields(getAoiHierarchyLevelMetadata().getAoiHierarchyLevelName());
		setAoiFields(aoiFields);
		
		initUserDefinedFields();
	}

	
	
}
