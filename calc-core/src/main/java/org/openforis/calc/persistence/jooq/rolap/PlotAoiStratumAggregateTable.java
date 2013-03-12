package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class PlotAoiStratumAggregateTable extends AoiAggregateTable<PlotFactTable> {

	private static final long serialVersionUID = 1L;
	
	private static final String INFIX_SUFFIX = "_stratum";

	private List<Field<Integer>> aoiFields;

	private AoiHierarchyLevelMetadata aoiHierarchyLevelMetadata;

	PlotAoiStratumAggregateTable(PlotFactTable factTable, AoiHierarchyLevelMetadata aoiHierarchyLevel) {
		super(factTable, getInfix(aoiHierarchyLevel.getAoiHierarchyLevelName()));
		this.aoiHierarchyLevelMetadata = aoiHierarchyLevel;
		initFields();
	}

	private static String getInfix(String aoiLevel) {
		return aoiLevel + INFIX_SUFFIX;
	}
	
	protected void initFields() {
		PlotFactTable fact = getFactTable();
		createFixedDimensionField(fact.STRATUM_ID);
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
	
//	public String getAoiLevel() {
//		return aoiLevel;
//	}
}
