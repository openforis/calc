package org.openforis.calc.chain;

import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.metadata.VariableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link CalculationStep} instances
 * 
 * 
 * @author S. Ricci
 *
 */
@Service
public class CalculationStepService {
	
	@Autowired
	private CalculationStepDao calculationStepDao;

	@Autowired
	private VariableDao variableDao;
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Transactional
	public void delete(int stepId) {
		CalculationStep step = calculationStepDao.find(stepId);
		if ( step != null ) {
			Variable<?> outputVariable = step.getOutputVariable();
			int variableId = outputVariable.getId();
			if ( ! calculationStepDao.isVariableUsedInMultipleCalculationSteps(variableId) ) {
				deleteOutputVariable(variableId);
			}
			calculationStepDao.delete(stepId);
		}
	}

	private void deleteOutputVariable(int variableId) {
		Variable<?> variable = variableDao.find(variableId);
		if ( variable instanceof QuantitativeVariable ) {
			workspaceService.deleteVariable((QuantitativeVariable) variable, true);
		} else {
			String errorMessage = String.format("Quantitative variable expected associated, found %s", 
					variable.getClass().getName());
			throw new IllegalArgumentException(errorMessage);
		}
	}

}
