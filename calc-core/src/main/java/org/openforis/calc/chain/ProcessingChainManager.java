package org.openforis.calc.chain;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openforis.calc.engine.Worker.Status;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.engine.WorkspaceBackup;
import org.openforis.calc.engine.WorkspaceService;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.Sequences;
import org.openforis.calc.persistence.jooq.tables.daos.CalculationStepDao;
import org.openforis.calc.persistence.jooq.tables.daos.ProcessingChainDao;
import org.openforis.calc.psql.Psql;
import org.openforis.calc.schema.EntityDataViewDao;
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
public class ProcessingChainManager {
	
	@Autowired
	private WorkspaceService workspaceService;
	
	@Autowired
	private ProcessingChainDao processingChainDao;
	
	@Autowired
	private CalculationStepDao calculationStepDao;
	
	@Autowired
	private EntityDataViewDao entityDataViewDao;
	
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
	public void saveCalculationStep( CalculationStep step , boolean resetResults ) {
		if ( step.getId() == null ) {
			Long nextval = psql.nextval( Sequences.CALCULATION_STEP_ID_SEQ );
			step.setId( nextval.intValue() );
			
			calculationStepDao.insert(step);
		} else {
			calculationStepDao.update(step);
		}
		
		updateProcessingChainStatus( step.getProcessingChain() , Status.PENDING );
		
		if( resetResults ){
			workspaceService.resetResult( step.getOutputVariable() );
		}
		
	}
	@Transactional
	public void saveCalculationStep( CalculationStep step ) {
		this.saveCalculationStep( step , true );
	}
	
	public CalculationStep loadCalculationStep( int stepId ){
		return calculationStepDao.fetchOneById( stepId );
	}
	
	@Transactional
	public void deleteCalculationSteps( ProcessingChain processingChain ){
		calculationStepDao.delete( processingChain.getCalculationSteps() );
		processingChain.clearCalculationSteps();
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
		
		// 1. delete output variable
		Variable<?> outputVariable = step.getOutputVariable();
		Entity entity = outputVariable.getEntity();
		// for now delete only categorical variable
		Integer deletedVariable = null;
		List<CalculationStep> steps = calculationStepDao.fetchByOutputVariableId(outputVariable.getId());
		if( steps.size() == 1 ){
			// set output var to null (foreign key constraint "calculation_step_variable_fkey" )
			step.setOutputVariableId(null);
			calculationStepDao.update(step);
			
			deletedVariable = outputVariable.getId();
			workspaceService.deleteVariable(outputVariable, false);
		}
		// 2. delete step from db
		calculationStepDao.delete( step );
		// 3. remove step from metadata
		processingChain.removeCalculationStep( step );
		// 4. update entity view
		entityDataViewDao.createOrUpdateView( entity );
		
		updateProcessingChainStatus( processingChain, Status.PENDING );
		
		// 4. update steps number in db
		updateCalculationSteps( processingChain );
				
		return deletedVariable;
	}
	
	@Transactional
	public void shiftCalculationStep(CalculationStep step, int stepNo) {
		ProcessingChain processingChain = step.getProcessingChain();
		processingChain.shiftStep(step, stepNo);
		
		updateCalculationSteps(processingChain);
	}

	protected void updateCalculationSteps(ProcessingChain processingChain) {
		for (CalculationStep calculationStep : processingChain.getCalculationSteps()) {
			calculationStepDao.update(calculationStep);
		}
	}

	public void updateProcessingChainStatus( ProcessingChain processingChain , Status status ){
		processingChain.setStatus( status );
		processingChainDao.update( processingChain );
	}
	
	@Transactional
	public void loadChains( Workspace workspace ){
		List<ProcessingChain> chains = processingChainDao.fetchByWorkspaceId( workspace.getId() );
		for (ProcessingChain chain : chains) {
			workspace.addProcessingChain(chain);
			loadSteps( chain );
		}
	}
	
	private void loadSteps( ProcessingChain chain ){
		List<CalculationStep> steps = calculationStepDao.fetchByChainId( chain.getId() );
		Collections.sort( steps, new Comparator<CalculationStep>() {
			@Override
			public int compare(CalculationStep o1, CalculationStep o2) {
				return o1.getStepNo().compareTo( o2.getStepNo() );
			}
		});
		for (CalculationStep step : steps) {
			chain.addCalculationStep( step );
			Workspace workspace = chain.getWorkspace();
			step.setOutputVariable( workspace.getVariableById(step.getOutputVariableId()) );
		}
		
	}

	@Transactional
	public void importBackup( Workspace workspace , WorkspaceBackup workspaceBackup ) {
		ProcessingChain processingChain = workspace.getDefaultProcessingChain();

		Workspace workspaceToImport = workspaceBackup.getWorkspace();
		
		List<CalculationStep> calculationSteps = workspaceToImport.getDefaultProcessingChain().getCalculationSteps();
		for ( CalculationStep calculationStep : calculationSteps ) {
			
			processingChain.addCalculationStep(calculationStep);
			
			calculationStep.setId( psql.nextval(Sequences.CALCULATION_STEP_ID_SEQ).intValue() );
			calculationStepDao.insert( calculationStep );
		}		
	}
	
	
	
}
