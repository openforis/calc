/**
 * 
 */
package org.openforis.calc.engine;

import java.util.HashSet;
import java.util.Set;

import org.jooq.Select;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.chain.CalculationStep.Type;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.Div;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.r.Sqldf;
import org.openforis.calc.schema.CategoryDimensionTable;

/**
 * @author Mino Togna
 * 
 */
public class CalculationStepRTask extends CalcRTask {

	private CalculationStep calculationStep;
	private RVariable dataFrame;
	private RVariable plotArea;

	private Set<String> outputVariables;
	// temp fix. 
	private Set<String> allOutputVariables;
	private RVariable connection;
	private CalcJob job;

	/**
	 * @param rEnvironment
	 * @param name
	 */
	public CalculationStepRTask(CalculationStep calculationStep, CalcJob job, RVariable connection, RVariable dataFrame, RVariable plotAreaVariable) {
		super( job.getrEnvironment(), calculationStep.getCaption());
		this.job = job;
		this.connection = connection;
		this.dataFrame = dataFrame;
		this.plotArea = plotAreaVariable;
		this.outputVariables = new HashSet<String>();
		this.allOutputVariables = new HashSet<String>();
		this.calculationStep = calculationStep;
		
		initScripts();
	}

//	public CalculationStepRTask(REnvironment rEnvironment, RVariable dataFrame, CalculationStep calculationStep) {
////		this(rEnvironment, dataFrame, calculationStep, null);
//	}

	private void initScripts() {
		RScript script = this.calculationStep.getRScript();
		
		SetValue setOutputValuePerHa = null;

		Variable<?> outputVariable 	= getOutputVariable();
		String variableName 		= outputVariable.getName();
		RVariable outputVar 		= r().variable( this.dataFrame, variableName );

		this.outputVariables.add(variableName);
		this.allOutputVariables.add(variableName);
		if( outputVariable instanceof CategoricalVariable ) {
			this.outputVariables.add(outputVariable.getInputCategoryIdColumn());
			this.allOutputVariables.add(outputVariable.getInputCategoryIdColumn());
		}
		
		// check if per/ha script needs to be added
		if ( this.plotArea != null && outputVariable instanceof QuantitativeVariable ) {
			String variablePerHaName = ( (QuantitativeVariable)outputVariable ).getVariablePerHaName();
			RVariable outputVarPerHa = r().variable(dataFrame, variablePerHaName);
			// set output variable per ha as result of output variable / plot
			// area
			Div valuePerHa 		= r().div(outputVar, plotArea);
			setOutputValuePerHa = r().setValue(outputVarPerHa, valuePerHa);

			this.allOutputVariables.add(variablePerHaName);
		}

		// assign also category class ids to data 
		SetValue convertCodeToCharacter = null;
		SetValue setCategoryClasses 	= null;
		SetValue setClassId				= null;
		if( this.calculationStep.getType() == Type.CATEGORY ){
			
			
			convertCodeToCharacter = r().setValue( outputVar ,  r().asCharacter( outputVar ) );
			
			MultiwayVariable variable = (MultiwayVariable) this.calculationStep.getOutputVariable();
			CategoryDimensionTable T = job.getSchemas().getDataSchema().getCategoryDimensionTable( variable  );
			if( T == null ){
				T = job.getSchemas().getExtendedSchema().getCategoryDimensionTable( variable );
			}
			
			Select<?> selectCategoryClasses = job.psql
				.select( T.getCodeField().as("code") , T.getIdField().as("id") )
				.from(T);
			RVariable categoryClasses = r().variable( "categoryClasses" );
			setCategoryClasses = r().setValue( categoryClasses, r().dbGetQuery(connection, selectCategoryClasses) );
			
			String select = "select c.id from " +this.dataFrame.toString() + " e left outer join categoryClasses c on e."+variableName + " = c.code";
			Sqldf selectClassIds = r().sqldf(select);
			RVariable classIdVariable = r().variable( this.dataFrame, outputVariable.getInputCategoryIdColumn() );
			
			setClassId = r().setValue( classIdVariable, r().variable(selectClassIds, "id") );
//			stand$stand_major_forest_status_id <- 
//			  sqldf( "select c.id from stand s left outer join categoryClasses c on s.stand_major_forest_status = c.code" )

		}
		
		// assign the result of the scripts (surrounded by a try statement)
		// execution to the variable result
		RVariable result = r().variable("result");
		SetValue setValue = r().setValue( result, r().rTry( script, convertCodeToCharacter , setOutputValuePerHa , setCategoryClasses , setClassId) );

		addScript( r().rScript("# ==================== " + calculationStep.getCaption() + " ====================") );
//		addScript( r().rScript("# ==========" ) );
//		addScript( r().rScript("# " + calculationStep.getCaption() ) );
//		addScript( r().rScript("# ==========" ) );
		addScript(setValue);
		addScript( r().checkError(result, connection) );
	}

	public Variable<?> getOutputVariable() {
		return calculationStep.getOutputVariable();
	}

	public Set<String> getInputVariables() {
		Set<String> variables = this.calculationStep.getRScript().getVariables();
		if( calculationStep.getType() == Type.CATEGORY ){
			// add id column as well
			variables.add( getOutputVariable().getInputCategoryIdColumn() );
		}
		return variables;
	}

	public Set<String> getOutputVariables() {
		return outputVariables;
	}
	
	public Set<String> getAllOutputVariables() {
		return allOutputVariables;
	}

}
