package org.openforis.calc.chain;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.CalculationException;
import org.openforis.calc.engine.ParameterMap;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.Equation;
import org.openforis.calc.metadata.EquationList;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.springframework.stereotype.Component;

/**
 * Utility class that generates the R script based on the calculation step type
 * @author Mino Togna
 *
 */
@Component
public class CalculationStepRScriptGenerator {

	public String generateRScript( CalculationStep calculationStep ){
		String script = null;
		
		switch( calculationStep.getType() ){
		case CATEGORY:
			script = createCategoryTypeScript( calculationStep );
			break;
		case EQUATION:
			script = createEquationTypeScript( calculationStep );
			break;
		case SCRIPT:
			script = calculationStep.getScript();
			break;
		
		}
		return script;
	}

	private String createEquationTypeScript( CalculationStep calculationStep ) {
		Workspace workspace = calculationStep.getWorkspace();
		
		Variable<?> outputVariable = calculationStep.getOutputVariable();
		Entity entity = outputVariable.getEntity();
		RVariable rOutputVariable = r().variable( entity.getName(), outputVariable.getName() );
		
		EquationList equationList = workspace.getEquationListById( calculationStep.getEquationListId() );
		Iterator<Equation> iterator = equationList.getEquations().iterator();
		RScript rScript = getNextREquation( iterator , calculationStep );
		
		SetValue setValue = r().setValue( rOutputVariable, rScript );
		
		String script = setValue.toString();
		return script;
	}

	private RScript getNextREquation( Iterator<Equation> iterator , CalculationStep calculationStep ) {
		Equation eq = iterator.next();
		
		String code = eq.getCode();
		String condition = eq.getCondition();
		
		StringBuffer sbCondition = new StringBuffer();
		RScript rCondition = null;
		if( StringUtils.isNotBlank( code ) || StringUtils.isNotBlank( condition)  ) {
			if(StringUtils.isNotBlank( code )) {
				
				Entity entity = calculationStep.getOutputVariable().getEntity();
				Integer codeVariableId = calculationStep.getParameters().getInteger( "codeVariable" );
				Variable<?> codeVariable = calculationStep.getWorkspace().getVariableById(codeVariableId );
				RVariable rCodeVariable	= r().variable( entity.getName() , codeVariable.getName() );
				
				sbCondition.append( rCodeVariable.toString() );
				sbCondition.append( " == " );
				if( codeVariable instanceof CategoricalVariable ){
					sbCondition.append( "'" );
				}
				sbCondition.append( code );
				if( codeVariable instanceof CategoricalVariable ){
					sbCondition.append( "'" );
				}
			}
			
			if(StringUtils.isNotBlank( condition )) {
				if( sbCondition.length() > 0 ) {
					sbCondition.append( " & " );
				}
				
				String expr = replaceVariables(condition , calculationStep);
				sbCondition.append( expr );
			}
			
			rCondition = r().rScript( sbCondition.toString() );
		}
		
		String equation = eq.getEquation();
		equation = replaceVariables(equation , calculationStep);
		RScript rEquation = r().rScript(equation);
		
		RScript rScript = null;
		if( iterator.hasNext() ) {
			if( rCondition == null ){
				throw new CalculationException( "Equation " + equation + " has neither code nor condition set" );
			}
			rScript = r().ifElse( rCondition, rEquation, getNextREquation(iterator , calculationStep) );
		} else {
			rScript = rEquation;
		}
		return rScript;
	}
	
	String replaceVariables( String string ,  CalculationStep calculationStep ) {
		Entity entity = calculationStep.getOutputVariable().getEntity();
		RVariable entityDf = r().variable( entity.getName() );

		String script = string;
		
		List<ParameterMap> varParams = calculationStep.getParameters().getList("variables");
		for (ParameterMap varParam : varParams) {
			Integer variableId = varParam.getInteger("variableId");
			Variable<?> variable = entity.getWorkspace().getVariableById(variableId);
			
			RVariable rVariable = r().variable( entityDf , variable.getName() );

			String equationVariable = varParam.getString( "equationVariable" );
			String rVar = rVariable.toString();
			rVar = rVar.replaceAll("\\$", "\\\\\\$");
			script = script.replaceAll( "\\b" + equationVariable + "\\b", rVar );
		}
		
		return script;
	}
	
	private String createCategoryTypeScript(CalculationStep calculationStep) {
		return null;
	}

	private RScript r() {
		return new RScript();
	}
}
