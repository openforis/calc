package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;
import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;

import org.jooq.Field;
import org.jooq.Result;
import org.jooq.impl.Factory;
import org.openforis.calc.model.Aoi;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.AoiRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Component 
@Transactional
public class AoiDao extends JooqDaoSupport<AoiRecord, Aoi> {
	
	private static final org.openforis.calc.persistence.jooq.tables.Aoi A = AOI;
	private static final org.openforis.calc.persistence.jooq.tables.AoiHierarchyLevel L = AOI_HIERARCHY_LEVEL;
	private static final org.openforis.calc.persistence.jooq.tables.AoiHierarchy H = AOI_HIERARCHY;
	
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
				.from(A)
				.join(L)
					.on(A.AOI_HIERARCHY_LEVEL_ID.eq(L.AOI_HIERARCHY_LEVEL_ID))
				.join(H)
					.on(L.AOI_HIERARCHY_ID.eq(H.AOI_HIERARCHY_ID))				
				.where(H.AOI_HIERARCHY_NAME.eq(hierarchyName))
				.fetch();
		
		return stream( result );
	}
	
}