/**
 * 
 */
package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.AoiHierarchyLevelMetadata;

/**
 * @author M. Togna
 *
 */
public abstract class AoiAggregateTable<T extends FactTable> extends AggregateTable<T> {

	private static final long serialVersionUID = 1L;
	
	AoiAggregateTable(T factTable, String infix) {
		super(factTable, infix);
	}
	
	public abstract AoiHierarchyLevelMetadata getAoiHierarchyLevelMetadata();

}
