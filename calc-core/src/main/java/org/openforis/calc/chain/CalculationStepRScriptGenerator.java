package org.openforis.calc.chain;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
			script = calculationStep.getScript(); //= createCategoryTypeScript( calculationStep );
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

	private RScript r() {
		return new RScript();
	}
	
	private String createEquationTypeScript( CalculationStep calculationStep ) {
		Workspace workspace 		= calculationStep.getWorkspace();
		
		EquationList equationList 	= workspace.getEquationListById( calculationStep.getEquationListId() );
		Iterator<Equation> iterator = equationList.getEquations().iterator();
		RScript rScript 			= getNextREquation( iterator , calculationStep );
		
		return rScript.toString();
	}
	
	private RScript getNextREquation( Iterator<Equation> iterator , CalculationStep calculationStep ) {
		RScript equationScript = new RScript();
		Equation eq = iterator.next();
		
		Variable<?> outputVariable 		= calculationStep.getOutputVariable();
		Entity entity 					= outputVariable.getEntity();
		
		RScript rCondition 				= getCondition( eq , calculationStep );
		
		RScript filteredDataFrame 		= r().rScript( entity.getName() + "["+rCondition.toScript()+" , ]" );
		String tmpDataFrameName 		= "tmp";
		RVariable tmp 					= r().variable(tmpDataFrameName);
		SetValue setTmp 				= r().setValue( tmp, filteredDataFrame );
		equationScript.addScript( setTmp );
		
		RVariable rOutputVar 		= r().variable(filteredDataFrame, outputVariable.getName() );
		String eqString 			= replaceVariables( eq.getEquation(), calculationStep, tmpDataFrameName, entity.getWorkspace() );
		SetValue setValue			= r().setValue(rOutputVar, r().rScript(eqString) );
		equationScript.addScript(setValue);
		
		RScript rScript = null;
		if( iterator.hasNext() ) {
//			if( rCondition == null ){
//				throw new CalculationException( "Equation " + equation + " has neither code nor condition set" );
//			}
			rScript = getNextREquation(iterator , calculationStep);
			equationScript.addScript( rScript );
		} else {
			rScript = equationScript;
		}
		
		return equationScript;
	}
	
	private RScript getCondition(Equation equation,  CalculationStep calculationStep) {
		RScript rCondition 				= null;
		StringBuffer sbCondition 		= new StringBuffer();
		Variable<?> outputVariable 		= calculationStep.getOutputVariable();
		Entity entity 					= outputVariable.getEntity();
		String code 					= equation.getCode();
		String condition				= equation.getCondition();
		
		if( StringUtils.isNotBlank( code ) || StringUtils.isNotBlank( condition)  ) {
			if(StringUtils.isNotBlank( code )) {
				
				Integer codeVariableId 		= calculationStep.getParameters().getInteger( "codeVariable" );
				Variable<?> codeVariable 	= calculationStep.getWorkspace().getVariableById(codeVariableId );
				RVariable rCodeVariable		= r().variable( entity.getName() , codeVariable.getName() );
				
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
		} else {
			RVariable rOutputVar = r().variable( entity.getName(), outputVariable.getName() );
			rCondition = r().not( r().isNa(rOutputVar) );
		}
		
		return rCondition;
	}

	private String replaceVariables( String script ,  CalculationStep calculationStep ) {
		Entity entity 		= calculationStep.getOutputVariable().getEntity();
		String entityName 	= entity.getName();
		Workspace workspace = entity.getWorkspace();
		
		return replaceVariables(script, calculationStep, entityName, workspace);
	}

	private String replaceVariables(String script, CalculationStep calculationStep, String entityName, Workspace workspace) {
		RVariable entityDf 	= r().variable( entityName );

		List<ParameterMap> varParams = calculationStep.getParameters().getList("variables");
		for (ParameterMap varParam : varParams) {
			Integer variableId = varParam.getInteger("variableId");
			Variable<?> variable = workspace.getVariableById(variableId);
			
			RVariable rVariable = r().variable( entityDf , variable.getName() );

			String equationVariable = varParam.getString( "equationVariable" );
			String rVar = rVariable.toString();
			rVar = rVar.replaceAll("\\$", "\\\\\\$");
			script = script.replaceAll( "\\b" + equationVariable + "\\b", rVar );
		}
		
		return script;
	}
	
}
