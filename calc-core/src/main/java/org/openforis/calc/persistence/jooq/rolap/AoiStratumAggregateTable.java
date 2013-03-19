/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.AoiHierarchyLevelMetadata;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author M. Togna
 * 
 */
public abstract class AoiStratumAggregateTable<T extends FactTable> extends AoiAggregateTable<T> {

	private static final long serialVersionUID = 1L;
	private static final String INFIX_SUFFIX = "_stratum";

	private AoiHierarchyLevelMetadata aoiHierarchyLevelMetadata;
	private List<? extends Field<Integer>> aoiFields;

	AoiStratumAggregateTable(T factTable, AoiHierarchyLevelMetadata aoiHierarchyLevel) {
		super(factTable, getInfix(aoiHierarchyLevel.getAoiHierarchyLevelName()));
		this.aoiHierarchyLevelMetadata = aoiHierarchyLevel;
		initFields();
	}

	abstract protected void initFields();

	@Override
	public AoiHierarchyLevelMetadata getAoiHierarchyLevelMetadata() {
		return aoiHierarchyLevelMetadata;
	}

	public List<Field<Integer>> getAoiFields() {
		return CollectionUtils.unmodifiableList(aoiFields);
	}

	protected void setAoiFields(List<? extends Field<Integer>> aoiFields) {
		this.aoiFields = aoiFields;
	}

	private static String getInfix(String aoiLevel) {
		return aoiLevel + INFIX_SUFFIX;
	}

}
