package org.openforis.calc.chain;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.openforis.calc.common.UserObject;
import org.openforis.calc.engine.ParameterHashMap;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.json.ParameterMapJsonSerializer;
import org.openforis.calc.metadata.Variable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A single user-defined step in a {@link ProcessingChain}
 * 
 * @author G. Miceli
 * @author S. Ricci
 * 
 */
@javax.persistence.Entity
@Table(name = "calculation_step")
public class CalculationStep extends UserObject {
	
	private static final String VARIABLE_PLACEMARK = "\\$(.+?)\\$";
	
	@Column(name = "module_name")
	private String moduleName;
	
	@Column(name = "module_version")
	private String moduleVersion;
	
	@Column(name = "operation_name")
	private String operationName;
		
	@Column(name = "step_no")
	private int stepNo;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@Fetch(value = FetchMode.SELECT)
	@JoinColumn(name = "output_variable_id")
	private Variable<?> outputVariable;
	
	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "chain_id")
	private ProcessingChain processingChain;
	
	@JsonSerialize(using = ParameterMapJsonSerializer.class)
	@Type(type="org.openforis.calc.persistence.hibernate.JsonParameterMapType")
	@Column(name = "parameters")
	private ParameterMap parameters;

	@Column(name = "script")
	private String script;

	public CalculationStep() {
		this.parameters = new ParameterHashMap();
	}
	
	public Integer getOutputVariableId() {
		return outputVariable == null ? null: outputVariable.getId();
	}
	
	public Integer getOutputEntityId() {
		return outputVariable == null ? null: outputVariable.getEntity().getId();
	}

	public ProcessingChain getProcessingChain() {
		return this.processingChain;
	}

	public ParameterMap parameters() {
		return this.parameters;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return this.moduleName;
	}

	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public String getModuleVersion() {
		return this.moduleVersion;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getOperationName() {
		return this.operationName;
	}

	public void setStepNo(int index) {
		this.stepNo = index;
	}

	public int getStepNo() {
		return this.stepNo;
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
	
	public String getScript() {
		return script;
	}
	
	public void setScript(String script) {
		this.script = script;
	}		
	
	@Override
	public String toString() {
		return String.format("#%d: %s:%s:%s", stepNo, moduleName, operationName, moduleVersion);
	}
	
	public Set<String> getInputVariables() {
		Set<String> variables = new HashSet<String>();
		Pattern p = Pattern.compile(VARIABLE_PLACEMARK);
		Matcher m = p.matcher(getScript());
		while (m.find()) {
			String variable = m.group(1);
			variables.add(variable);
		}
		
		return variables;
	}
	
	public Set<String> getVariables() {
		Set<String> variables = getInputVariables();
		variables.add(getOutputVariable().getName());
		return variables;
	}
}