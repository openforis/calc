package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author M. Togna
 * 
 */
public class PlotAoiAggregateTable extends AoiAggregateTable<PlotAoiStratumAggregateTable> {

	private static final long serialVersionUID = 1L;

	private List<Field<Integer>> aoiFields;

	private AoiHierarchyLevelMetadata aoiHierarchyLevelMetadata;

	private PlotAoiStratumAggregateTable stratumAggTable;

	PlotAoiAggregateTable(PlotAoiStratumAggregateTable factTable, AoiHierarchyLevelMetadata aoiHierarchyLevel, PlotAoiStratumAggregateTable stratumAggTable) {
		super(factTable, aoiHierarchyLevel.getAoiHierarchyLevelName());
		this.aoiHierarchyLevelMetadata = aoiHierarchyLevel;
		this.stratumAggTable = stratumAggTable;
		initFields();
	}

	protected void initFields() {
		PlotAoiStratumAggregateTable agg = getFactTable();
		PlotFactTable fact = agg.getFactTable();
		// createFixedDimensionField(fact.STRATUM_ID);
		createFixedMeasureField(fact.EST_AREA);
		aoiFields = createAoiFields(aoiHierarchyLevelMetadata.getAoiHierarchyLevelName());
		initUserDefinedFields();
	}

	public List<Field<Integer>> getAoiFields() {
		return CollectionUtils.unmodifiableList(aoiFields);
	}

	@Override
	public AoiHierarchyLevelMetadata getAoiHierarchyLevelMetadata() {
		return aoiHierarchyLevelMetadata;
	}

	public PlotAoiStratumAggregateTable getStratumAggTable() {
		return stratumAggTable;
	}

}
