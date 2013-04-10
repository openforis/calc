package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;

/**
 * 
 * @author M. Togna
 * 
 */
public class SpecimenAoiAggregateTable extends AoiAggregateTable<SpecimenAoiStratumAggregateTable> {

	private static final long serialVersionUID = 1L;
	private AoiHierarchyLevelMetadata aoiHierarchyLevelMetadata;
	private SpecimenAoiStratumAggregateTable stratumAggTable;
	private List<Field<Integer>> aoiFields;

	SpecimenAoiAggregateTable(SpecimenAoiStratumAggregateTable factTable, SpecimenAoiStratumAggregateTable stratumAggTable) {
		super(factTable, factTable.getAoiHierarchyLevelMetadata().getAoiHierarchyLevelName());
		this.aoiHierarchyLevelMetadata = factTable.getAoiHierarchyLevelMetadata();
		this.stratumAggTable = stratumAggTable;
		initFields();
	}

	// @Override
	protected void initFields() {
		SpecimenPlotAggregateTable fact = getFactTable().getFactTable();
		SpecimenFactTable specimenFact = fact.getFactTable();

		// createFixedDimensionField(specimenFact.STRATUM_ID);
		createFixedDimensionField(specimenFact.SPECIMEN_TAXON_ID);
		
		aoiFields = createAoiFields(getAoiHierarchyLevelMetadata().getAoiHierarchyLevelName());

		initUserDefinedFields();		
		createUserDefinedMeasureField(specimenFact.COUNT_EST.getName());
	}

	@Override
	public AoiHierarchyLevelMetadata getAoiHierarchyLevelMetadata() {
		return aoiHierarchyLevelMetadata;
	}

	public List<Field<Integer>> getAoiFields() {
		return aoiFields;
	}
	
	public SpecimenAoiStratumAggregateTable getStratumAggTable() {
		return stratumAggTable;
	}

}
