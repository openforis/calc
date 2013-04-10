package org.openforis.calc.persistence.jooq.rolap;

import static org.openforis.calc.persistence.jooq.Tables.PLOT_SECTION;
import static org.openforis.calc.persistence.jooq.Tables.SPECIMEN_VIEW;

import java.math.BigDecimal;
import java.util.List;

import org.jooq.Field;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.persistence.jooq.tables.SpecimenView;
import org.openforis.commons.collection.CollectionUtils;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class SpecimenFactTable extends FactTable {

	private static final long serialVersionUID = 1L;
	private static final SpecimenView S = SPECIMEN_VIEW;

	public static final String MEASURE_PLOT_SECTION_AREA = PLOT_SECTION.PLOT_SECTION_AREA.getName();
	public static final String MEASURE_INCLUSION_AREA = S.INCLUSION_AREA.getName();

	// Fixed dimensions
	public final Field<Integer> STRATUM_ID = createFixedDimensionField(S.STRATUM_ID);
	public final Field<Integer> CLUSTER_ID = createFixedDimensionField(S.CLUSTER_ID);
	public final Field<Integer> PLOT_ID = createFixedDimensionField("plot_id");
	public final Field<Integer> SPECIMEN_ID = createFixedDimensionField(S.SPECIMEN_ID);
	public final Field<Integer> SPECIMEN_TAXON_ID = createFixedDimensionField(S.SPECIMEN_TAXON_ID);

	public final Field<BigDecimal> INCLUSION_AREA = createFixedMeasureField(MEASURE_INCLUSION_AREA);
	public final Field<BigDecimal> PLOT_SECTION_AREA = createFixedMeasureField(MEASURE_PLOT_SECTION_AREA);
	public Field<BigDecimal> COUNT_EST;
	
	private List<Field<Integer>> aoiFields;

	SpecimenFactTable(String schema, ObservationUnitMetadata unit) {
		super(schema, unit.getFactTableName(), unit);
		initFields();
	}

	protected void initFields() {
		aoiFields = createAoiFields();
		initUserDefinedFields();
		COUNT_EST = createUserDefinedMeasureField("count_est");
	}

	public List<Field<Integer>> getAoiFields() {
		return CollectionUtils.unmodifiableList(aoiFields);
	}

}
