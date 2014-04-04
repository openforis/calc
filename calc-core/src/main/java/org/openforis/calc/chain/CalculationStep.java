package org.openforis.calc.chain;

import java.util.Set;

import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.tables.pojos.CalculationStepBase;
import org.openforis.calc.r.RScript;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A single user-defined step in a {@link ProcessingChain}
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
public class CalculationStep extends CalculationStepBase {

	private static final long serialVersionUID = 1L;

	public static final String VARIABLE_PATTERN = "\\$(.+?)\\$";


	@JsonIgnore
	private Variable<?> outputVariable;

	@JsonIgnore
	private ProcessingChain processingChain;


	public CalculationStep() {
		setParameters( new ParameterHashMap() );
	}

//	public Integer getOutputEntityId() {
//		return outputVariable == null ? null : outputVariable.getEntity().getId();
//	}

	public ProcessingChain getProcessingChain() {
		return this.processingChain;
	}

	public Variable<?> getOutputVariable() {
		return outputVariable;
	}

	public void setOutputVariable(Variable<?> outputVariable) {
		this.outputVariable = outputVariable;
	}

	public void setProcessingChain(ProcessingChain chain) {
		this.processingChain = chain;
	}

	@Override
	public String toString() {
		return String.format( "#%d: %s:%s:%s", getStepNo(), getModuleName(), getOperationName(), getModuleVersion() );
	}

	public RScript getRScript() {
		Variable<?> outputVariable = this.getOutputVariable();
		Entity entity = outputVariable.getEntity();
		return new RScript().rScript( getScript(), entity.getHierarchyVariables() );
	}

	/**
	 * Convenience method. left here for backwards compatibility. Use
	 * getRScript().getVariables() to get variables from script
	 * 
	 * @return
	 */
	@Deprecated
	public Set<String> getInputVariables() {
		return getRScript().getVariables();
	}

	public Set<String> getVariables() {
		Set<String> variables = getInputVariables();
		variables.add(getOutputVariable().getName());
		return variables;
	}
}