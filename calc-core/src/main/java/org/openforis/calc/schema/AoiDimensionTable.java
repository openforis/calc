package org.openforis.calc.schema;

import static org.openforis.calc.persistence.jooq.Tables.AOI;

import java.math.BigDecimal;

import org.jooq.Record;
import org.jooq.TableField;
import org.openforis.calc.metadata.AoiLevel;

/**
 * 
 * @author M. Togna
 * 
 */
public class AoiDimensionTable extends DimensionTable {

	private static final long serialVersionUID = 1L;

	public final TableField<Record, Integer> AOI_LEVEL_ID = copyField(AOI.AOI_LEVEL_ID);
	public final TableField<Record, Integer> PARENT_AOI_ID = copyField(AOI.PARENT_AOI_ID);
	public final TableField<Record, String> SHAPE = copyField(AOI.SHAPE);
	public final TableField<Record, BigDecimal> TOTAL_AREA = copyField(AOI.TOTAL_AREA);
	public final TableField<Record, BigDecimal> LAND_AREA = copyField(AOI.LAND_AREA);

	private AoiLevel hierarchyLevel;

	AoiDimensionTable(RelationalSchema schema, AoiLevel aoiHierarchyLevel) {
		super(aoiHierarchyLevel.getDimensionTable(), schema);
		this.hierarchyLevel = aoiHierarchyLevel;
	}

	public AoiLevel getHierarchyLevel() {
		return hierarchyLevel;
	}
}
