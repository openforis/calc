package org.openforis.calc.chain;

import org.openforis.calc.engine.Worker.Status;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.calc.persistence.jooq.tables.ProcessingChainTable;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.ProcessingChainDao;
import org.openforis.calc.psql.Psql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages {@link CalculationStep} instances
 * 
 * 
 * @author S. Ricci
 * @author Mino Togna
 * 
 */
@Service
public class ProcessingChainService {
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired
	private CalculationStepDao calculationStepDao;
	
	@Autowired
	private Psql psql;
	
	@Transactional
	public void saveProcessingChain(ProcessingChain chain) {
		if ( chain.getId() == null ) {
			Long nextval = psql.nextval( Sequences.PROCESSING_CHAIN_ID_SEQ );
			chain.setId( nextval.intValue() );
			
			processingChainDao.insert(chain);
		} else {
			processingChainDao.update(chain);
		}
	}
	
	@Transactional
	public void createDefaultProcessingChain( Workspace ws ) {
		ProcessingChain chain = new ProcessingChain();
		chain.setCaption(Workspace.DEFAULT_CHAIN_CAPTION);
		ws.addProcessingChain(chain);
		
		saveProcessingChain( chain );
	}
	
	@Transactional
	public void saveCalculationStep( CalculationStep step ) {
		if ( step.getId() == null ) {
			Long nextval = psql.nextval( Sequences.CALCULATION_STEP_ID_SEQ );
			step.setId( nextval.intValue() );
			
			calculationStepDao.insert(step);
		} else {
			calculationStepDao.update(step);
		}
		
		workspaceService.resetResult( step.getOutputVariable() );
	}
	
	/**
	 * Delete the step with given stepId 
	 * Returns the output variable id if it has been removed because it's used only by the deleted calculation step
	 *   
	 * @param stepId
	 */
	@Transactional
	public Integer deleteCalculationStep(CalculationStep step) {		
		ProcessingChain processingChain = step.getProcessingChain();
		
		// 1. delete step from db
		calculationStepDao.delete( step );
		
//		QuantitativeVariable outputVariable = (QuantitativeVariable) step.getOutputVariable();
		// 2. delete output variable if defined only for this step. disabeld now
		Integer deletedVariable = null;		
//		if ( outputVariable.isUserDefined() ) {
//			Workspace ws = processingChain.getWorkspace();
//			List<CalculationStep> steps = ws.getCalculationStepsByVariable( outputVariable.getId() );
//			if ( steps.size() == 1 && steps.get(0).getId().equals(step.getId()) ) {
//				deletedVariable = outputVariable.getId();
//				workspaceService.deleteOutputVariable(outputVariable, true);
//			}
//		}
		
				
		// 3. remove step from metadata
		processingChain.removeCalculationStep( step );
		
		// 4. update steps number in db
		saveCalculationSteps( processingChain );
				
		return deletedVariable;
	}
	
	@Transactional
	public void shiftCalculationStep(CalculationStep step, int stepNo) {
		ProcessingChain processingChain = step.getProcessingChain();
		processingChain.shiftStep(step, stepNo);
		
		saveCalculationSteps(processingChain);
	}

	protected void saveCalculationSteps(ProcessingChain processingChain) {
		for (CalculationStep calculationStep : processingChain.getCalculationSteps()) {
			calculationStepDao.update(calculationStep);
		}
	}

	public void updateProcessingChainStatus( ProcessingChain processingChain , Status status ){
		processingChain.setStatus( status );
		
		ProcessingChainTable T = Tables.PROCESSING_CHAIN;
		psql
			.update( T )
			.set( T.STATUS , status )
			.where( T.ID.eq(processingChain.getId()) )
			.execute();
	}
	
}
