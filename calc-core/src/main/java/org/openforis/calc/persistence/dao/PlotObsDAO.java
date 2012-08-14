/**
 * 
 */
package org.openforis.calc.persistence.dao;

import java.util.List;

import javax.persistence.Query;

import org.openforis.calc.model.PlotObs;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 * 
 */
public class PlotObsDAO extends AbstractDAO {

	@Transactional
	@SuppressWarnings("unchecked")
	public List<PlotObs> getAll() {
		Query query = createQuery("SELECT p FROM " + PlotObs.class.getName() + " p");
		List<PlotObs> plots = query.getResultList();
		return plots;
	}

}
