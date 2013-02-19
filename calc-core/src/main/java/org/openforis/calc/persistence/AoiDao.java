package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;

import org.jooq.Field;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.Aoi;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.AoiRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author Mino Togna
 */
@Component 
@Transactional
public class AoiDao extends JooqDaoSupport<AoiRecord, Aoi> {

	public AoiDao() {
		super(AOI, Aoi.class);
	}

	public FlatDataStream streamByHierarchyName(String[] fieldNames, String hierarchyName) {
		Field<?>[] fields = getFields(fieldNames);
		if(fields == null || fields.length == 0) {
			fields = AOI.getFields().toArray(new Field[AOI.getFields().size()]);
		}
		Factory create = getJooqFactory();
		
		Result<?> result = 
				create
				.select(fields)
				.from(AOI)
				.join(AOI_HIERARCHY_LEVEL)
					.on(AOI.AOI_HIERARCHY_LEVEL_ID.eq(AOI_HIERARCHY_LEVEL.AOI_HIERARCHY_LEVEL_ID))
				.join(AOI_HIERARCHY)
					.on(AOI_HIERARCHY_LEVEL.AOI_HIERARCHY_ID.eq(AOI_HIERARCHY.AOI_HIERARCHY_ID))				
				.where(AOI_HIERARCHY.AOI_HIERARCHY_NAME.eq(hierarchyName))
				.fetch();
		
		return stream( result );
	}
}