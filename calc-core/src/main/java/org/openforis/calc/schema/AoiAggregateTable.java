package org.openforis.calc.schema;

import org.openforis.calc.metadata.AoiLevel;
import org.openforis.calc.metadata.Entity;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 * 
 */
public class AoiAggregateTable extends AggregateTable {

	private static final long serialVersionUID = 1L;
	private static final String TABLE_NAME_FORMAT = "_%s_%s_agg";
	
	AoiAggregateTable(DataTable sourceTable, AoiLevel aoiLevel) {
		super(sourceTable, getName(sourceTable.getEntity(), aoiLevel), aoiLevel );
	}
	
	private static String getName(Entity entity, AoiLevel aoiLevel) {
		return String.format(TABLE_NAME_FORMAT, entity.getName(), aoiLevel.getNormalizedName() );
	}

}
