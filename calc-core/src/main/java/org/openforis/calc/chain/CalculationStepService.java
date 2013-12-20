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
	
	/**
	 * Delete the step with given stepId 
	 * Returns the output variable id if it has been removed because it's used only by the deleted calculation step
	 *   
	 * @param stepId
	 */
	@Transactional
	public Integer delete(int stepId) {
		CalculationStep step = calculationStepDao.find(stepId);
		Integer deletedVariable = null;
		Variable<?> outputVariable = step.getOutputVariable();
		calculationStepDao.delete(stepId);
		Integer chainId = step.getProcessingChain().getId();
		calculationStepDao.decrementFollowingStepNumbers(chainId, step.getStepNo());
		if ( outputVariable.isUserDefined() ) {
			int variableId = outputVariable.getId();
			if ( calculationStepDao.countOutputVariableSteps(variableId) == 0) {
				deletedVariable = variableId;
				deleteOutputVariable(variableId);
			}
		}
		return deletedVariable;
	}

	private void deleteOutputVariable(int variableId) {
		Variable<?> variable = variableDao.find(variableId);
		if ( variable instanceof QuantitativeVariable ) {
			workspaceService.deleteOutputVariable((QuantitativeVariable) variable, true);
		} else {
			String errorMessage = String.format("Quantitative variable expected associated, found %s", 
					variable.getClass().getName());
			throw new IllegalArgumentException(errorMessage);
		}
	}

	@Transactional
	public void updateStepNumber(int stepId, int stepNo) {
		CalculationStep step = calculationStepDao.find(stepId);
		ProcessingChain processingChain = step.getProcessingChain();
		Integer chainId = processingChain.getId();
		int oldStepNo = step.getStepNo();
		calculationStepDao.decrementFollowingStepNumbers(chainId, oldStepNo);
		calculationStepDao.incrementFollowingStepNumbers(chainId, stepNo);
		step.setStepNo(stepNo);
		calculationStepDao.update(step);
	}

}
