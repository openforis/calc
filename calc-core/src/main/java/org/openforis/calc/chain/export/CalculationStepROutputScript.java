package org.openforis.calc.chain.export;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Select;
import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.MultiwayVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.metadata.Variable;
import org.openforis.calc.r.Div;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.r.Sqldf;
import org.openforis.calc.schema.CategoryDimensionTable;
import org.openforis.calc.schema.ResultTable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class CalculationStepROutputScript extends ROutputScript {

	private static final String NAME_FORMAT = "%s-%s.R";
	
	private RScript postScript ;
	
	public CalculationStepROutputScript(int index , CalculationStep calculationStep , Schemas schemas ) {
		super ( getName(calculationStep), createScript(calculationStep , schemas), Type.USER , index );
		this.postScript = initPostScript( calculationStep , schemas );
	}

	private static String getName(CalculationStep calculationStep) {
		Variable<?> variable 	= calculationStep.getOutputVariable();
		Entity entity 			= variable.getEntity();
		
		return String.format( NAME_FORMAT, entity.getName() , variable.getName() );
	}

	private static RScript createScript( CalculationStep calculationStep, Schemas schemas  ) {
		RScript r 				= r();
		
		Variable<?> variable 	= calculationStep.getOutputVariable();
		Entity entity 			= variable.getEntity();
		
		String script 			= calculationStep.getScript();

		RVariable rVariable 	= r().variable( entity.getName(), variable.getName() );
		
		if( StringUtils.isBlank(script) ){
			SetValue setValue 		= r().setValue( rVariable, r().rScript("1") );
			
			r.addScript( setValue );
		} else {
			r.addScript( r().rScript( script ) );
		}
		
		return r;
	}
	
	private RScript initPostScript( CalculationStep calculationStep, Schemas schemas  ) {
		RScript r 				= r();
		
		Variable<?> variable 	= calculationStep.getOutputVariable();
		Entity entity 			= variable.getEntity();
		Workspace workspace 	= entity.getWorkspace();
		
		RVariable dataFrame		= r().variable( entity.getName() );
		RVariable rVariable 	= r().variable( entity.getName(), variable.getName() );
		
		// add variable/plot_area formula in case it's a qty variable and a sampling strategy has been set
		if( workspace.hasSamplingDesign() && variable instanceof QuantitativeVariable && entity.isInSamplingUnitHierarchy() ){
			
			String variablePerHaName 	= ( (QuantitativeVariable) variable ).getVariablePerHaName();
			RVariable variablePerHa 	= r().variable( dataFrame, variablePerHaName );
			RVariable plotAreaVariable 	= r().variable( dataFrame, ResultTable.PLOT_AREA_COLUMN_NAME );
			
			// set output variable per ha as result of output variable / plot_area
			Div valuePerHa 					= r().div( rVariable, plotAreaVariable );
			SetValue setOutputValuePerHa 	= r().setValue( variablePerHa, valuePerHa );
			
			r.addScript( setOutputValuePerHa );
		}
		
		if( calculationStep.getType() == org.openforis.calc.chain.CalculationStep.Type.CATEGORY ){
			
			SetValue convertCodeToCharacter = r().setValue( rVariable ,  r().asCharacter( rVariable ) );
			r.addScript( convertCodeToCharacter );
				
			CategoryDimensionTable T 			= schemas.getDataSchema().getCategoryDimensionTable( (MultiwayVariable)variable  );
			if( T == null ){
				T = schemas.getExtendedSchema().getCategoryDimensionTable( (MultiwayVariable)variable );
			}
			Select<?> selectCategoryClasses  	= psql().select( T.getCodeField().as("code") , T.getIdField().as("id") ).from( T );
			RVariable categoryClasses 			= r().variable( "categoryClasses" );
			SetValue setCategoryClasses 		= r().setValue( categoryClasses, r().dbGetQuery( ROutputScript.CONNECTION_VAR , selectCategoryClasses) );
			r.addScript( setCategoryClasses );
			
			String select 				= "select c.id from " +dataFrame.toString() + " e left outer join categoryClasses c on e."+ variable.getName() + " = c.code";
			Sqldf selectClassIds 		= r().sqldf( select );
			RVariable classIdVariable 	= r().variable( dataFrame, variable.getInputCategoryIdColumn() );
			SetValue setClassId 		= r().setValue( classIdVariable, r().variable(selectClassIds, "id") );
			r.addScript( setClassId );
		}
		
		return r;
	}
	
	public RScript getPostExecScript() {
		return postScript;
	}	

}
