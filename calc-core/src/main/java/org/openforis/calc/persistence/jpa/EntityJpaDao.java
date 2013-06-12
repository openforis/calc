package org.openforis.calc.persistence.jpa;

import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.EntityDao;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class EntityJpaDao extends AbstractJpaDao<Entity> implements EntityDao {

}
