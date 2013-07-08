package org.openforis.calc.engine;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 */
@Service
public class CalculationStepManager {

	@Autowired
	private CalculationStepDao dao;

	@Transactional
	public void save(CalculationStep step) {
		dao.save(step);
	}

	@Transactional
	public void saveAll(List<CalculationStep> steps) {
		for ( CalculationStep step : steps ) {
			System.out.println(step.getProcessingChain().getId() + " " + step.getStepNo());
			save(step);
		}
	}

	@Transactional
	public CalculationStep get(int stepId) {
		return dao.find(stepId);
	}
}
