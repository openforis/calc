package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY_LEVEL;

import java.util.List;

import org.openforis.calc.model.AoiHierarchyLevel;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyLevelRecord;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 */
@Component
@Transactional
public class AoiHierarchyLevelDao extends JooqDaoSupport<AoiHierarchyLevelRecord, AoiHierarchyLevel> {

	public AoiHierarchyLevelDao() {
		super(AOI_HIERARCHY_LEVEL, AoiHierarchyLevel.class);
	}

	public FlatDataStream streamByName(String[] fieldNames, String name) {
		return stream(fieldNames, AOI_HIERARCHY_LEVEL.AOI_HIERARCHY_LEVEL_NAME, name);
	}

	public List<AoiHierarchyLevel> findByHierarchyId(int id) {
        return getJooqFactory().selectFrom(AOI_HIERARCHY_LEVEL)
                .where(AOI_HIERARCHY_LEVEL.AOI_HIERARCHY_ID.eq(id))
                .orderBy(AOI_HIERARCHY_LEVEL.AOI_HIERARCHY_LEVEL_RANK)
                .fetch()
                .into(AoiHierarchyLevel.class);
	}
	
}
