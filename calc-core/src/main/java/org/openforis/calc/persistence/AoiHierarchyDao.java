package org.openforis.calc.persistence;

import static org.openforis.calc.persistence.jooq.Tables.AOI_HIERARCHY;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.AoiHierarchy;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.tables.records.AoiHierarchyRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 */
@Component
@Transactional
public class AoiHierarchyDao extends JooqDaoSupport<AoiHierarchyRecord, AoiHierarchy> {

	public AoiHierarchyDao() {
		super(AOI_HIERARCHY, AoiHierarchy.class);
	}

	public FlatDataStream streamByName(String[] fieldNames, String name) {
		return stream(fieldNames, AOI_HIERARCHY.AOI_HIERARCHY_NAME, name);
	}
	
}
