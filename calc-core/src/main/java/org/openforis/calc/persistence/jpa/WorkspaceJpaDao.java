package org.openforis.calc.persistence.jpa;

import org.openforis.calc.workspace.Workspace;
import org.openforis.calc.workspace.WorkspaceDao;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class WorkspaceJpaDao extends AbstractJpaDao<Workspace> implements WorkspaceDao {

}
