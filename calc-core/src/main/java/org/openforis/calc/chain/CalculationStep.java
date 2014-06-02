package org.openforis.calc.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.persistence.jooq.tables.pojos.CalculationStepBase;
import org.openforis.calc.r.RScript;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A single user-defined step in a {@link ProcessingChain}
 * 
 * @author G. Miceli
 * @author S. Ricci
 * @author Mino Togna
 * 
 */
public class CalculationStep extends CalculationStepBase {

	private static final long serialVersionUID = 1L;

	public enum Type {
		SCRIPT, EQUATION, CATEGORY
	}

	@JsonIgnore
	private Variable<?> outputVariable;

	@JsonIgnore
	private ProcessingChain processingChain;

	public CalculationStep() {
		setParameters(new ParameterHashMap());
	}

	public ProcessingChain getProcessingChain() {
		return this.processingChain;
	}

	public Variable<?> getOutputVariable() {
		return this.outputVariable;
	}

	public void setOutputVariable(Variable<?> outputVariable) {
		this.outputVariable = outputVariable;
		setOutputVariableId(outputVariable == null ? null : outputVariable.getId());
	}

	public Integer getOutputEntityId() {
		return outputVariable == null ? null : outputVariable.getEntityId();
	}

	public void setProcessingChain(ProcessingChain chain) {
		this.processingChain = chain;
		this.setChainId(chain == null ? null : chain.getId());
	}

	@Override
	public String toString() {
		return String.format("#%d: %s:%s:%s", getStepNo(), getModuleName(), getOperationName(), getModuleVersion());
	}

	public RScript getRScript() {
		Variable<?> outputVariable = this.getOutputVariable();
		Entity entity = outputVariable.getEntity();
		return new RScript().rScript(getScript(), entity.getHierarchyVariables());
	}

	public Set<String> getVariables() {
		Set<String> variables = getRScript().getVariables();
		variables.add(getOutputVariable().getName());
		return variables;
	}

	@Override
	@JsonIgnore
	public Integer getStepNo() {
		return super.getStepNo();
	}

	@JsonIgnore
	Workspace getWorkspace() {
		Workspace workspace = getProcessingChain().getWorkspace();
		return workspace;
	}

	@JsonIgnore
	public List<CalculationStepCategoryClassParameters> getCategoryClassParameters() {
		List<CalculationStepCategoryClassParameters> params = new ArrayList<CalculationStepCategoryClassParameters>();
		if( this.getType() == Type.CATEGORY ){
			ParameterMap map = getParameters();
			List<ParameterMap> list = map.getList( "categoryClassParameters" );
			for ( ParameterMap parameterMap : list) {
				CalculationStepCategoryClassParameters s = new CalculationStepCategoryClassParameters( (ParameterHashMap) parameterMap );
				params .add( s );
			}
		}
		return params;
	}
}