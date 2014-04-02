package org.openforis.calc.chain;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
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
	private WorkspaceService workspaceService;
	
	/**
	 * Delete the step with given stepId 
	 * Returns the output variable id if it has been removed because it's used only by the deleted calculation step
	 *   
	 * @param stepId
	 */
	@Transactional
	public Integer delete(Workspace ws, int stepId) {
		ProcessingChain processingChain = ws.getDefaultProcessingChain();
		CalculationStep step = processingChain.getCalculationStep(stepId);
		processingChain.removeStepById(stepId);
		calculationStepDao.delete(stepId);
		Variable<?> outputVariable = step.getOutputVariable();
		if ( outputVariable instanceof QuantitativeVariable ) {
			Integer deletedVariableId = null;
			if ( outputVariable.isUserDefined() ) {
				int variableId = outputVariable.getId();
				if ( calculationStepDao.countOutputVariableSteps(variableId) == 0) {
					workspaceService.deleteOutputVariable((QuantitativeVariable) outputVariable, true);
					deletedVariableId = variableId;
				}
			}
			return deletedVariableId;
		} else {
			String errorMessage = String.format("Quantitative variable expected associated, found %s", 
					outputVariable.getClass().getName());
			throw new IllegalArgumentException(errorMessage);
		}
	}
	
	@Transactional
	public void updateStepNumber(Workspace ws, int stepId, int stepNo) {
		ProcessingChain processingChain = ws.getDefaultProcessingChain();
		CalculationStep step = processingChain.getCalculationStep(stepId);
		processingChain.shiftStep(step, stepNo);
		//persist calculation step updates
		for (CalculationStep s : processingChain.getCalculationSteps()) {
			calculationStepDao.save(s);
		}
	}

}
