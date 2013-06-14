package org.openforis.calc.engine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public class CalculationStepService {
	@Autowired
	private CalculationStepDao calculationStepDao;

	@Transactional
	public void saveCalculationStep(CalculationStep step) {
		calculationStepDao.save(step);
	}
}
