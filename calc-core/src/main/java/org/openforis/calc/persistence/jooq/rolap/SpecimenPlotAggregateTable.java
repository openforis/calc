package org.openforis.calc.persistence.jooq.rolap;

import java.util.List;

import org.jooq.Field;
import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author M. Togna
 * 
 */
public class SpecimenPlotAggregateTable extends AggregateTable<SpecimenFactTable> {

	private static final long serialVersionUID = 1L;

	private static final String INFIX = "plot";

	private List<Field<Integer>> aoiFields;

	public Field<Integer> PLOT_FIELD;

	SpecimenPlotAggregateTable(SpecimenFactTable factTable) {
		super(factTable, INFIX);
		initFields();
	}

	protected void initFields() {
		SpecimenFactTable fact = getFactTable();
		createFixedDimensionField(fact.STRATUM_ID);
//		createFixedDimensionField(fact.CLUSTER_ID);
		createField(fact.CLUSTER_ID);
		PLOT_FIELD = createField(fact.PLOT_ID);
		createFixedDimensionField(fact.SPECIMEN_TAXON_ID);
		
		aoiFields = createAoiFields();

		initUserDefinedFields();
	}

	public List<Field<Integer>> getAoiFields() {
		return CollectionUtils.unmodifiableList(aoiFields);
	}

}
