package org.openforis.calc.persistence;

import org.openforis.calc.model.AoiHierarchy;
import org.openforis.calc.persistence.jooq.JooqDaoSupport;
import org.openforis.calc.persistence.jooq.Tables;
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
		super(Tables.AOI_HIERARCHY, AoiHierarchy.class);
	}
}