package org.openforis.calc.persistence.jooq.rolap;

import org.openforis.calc.model.TaxonomicChecklistMetadata;

/**
 * 
 * @author M. Togna
 * 
 */
public class TaxonDimensionTable extends HierarchicalDimensionTable {

	private static final long serialVersionUID = 1L;

	private TaxonomicChecklistMetadata taxonomicChecklistMetadata;

	
	TaxonDimensionTable(String schema, TaxonomicChecklistMetadata taxonomicChecklistMetadata, TaxonDimensionTable parentTable) {
		super(schema, taxonomicChecklistMetadata.getTableName() , parentTable);
		this.taxonomicChecklistMetadata = taxonomicChecklistMetadata;
	}

	public TaxonomicChecklistMetadata getTaxonomicChecklistMetadata() {
		return taxonomicChecklistMetadata;
	}
}
