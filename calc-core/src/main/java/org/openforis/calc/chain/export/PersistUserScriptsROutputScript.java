package org.openforis.calc.chain.export;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.r.CalcPersistBaseUnitWeightScript;
import org.openforis.calc.r.CalcPersistCalculationStepScript;
import org.openforis.calc.r.CalcPersistCommonScript;
import org.openforis.calc.r.CalcPersistEntityPlotAreaScript;
import org.openforis.calc.r.CalcPersistErrorScript;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.schema.Schemas;

/**
 * 
 * @author M. Togna
 *
 */
public class PersistUserScriptsROutputScript extends ROutputScript {

	public PersistUserScriptsROutputScript( int index, Schemas schemas, CommonROutputScript commonScript, BaseUnitWeightROutputScript weightScript,
			List<EntityPlotAreaROutputScript> plotAreaScripts, List<CalculationStepROutputScript> chainScripts, ErrorFunctionsROutputScript errorFunctionsScript ) {
		
		super( "persist-user-scripts.R", createScript(schemas, commonScript, weightScript, plotAreaScripts, chainScripts, errorFunctionsScript) , Type.SYSTEM , index );
	}

	private static RScript createScript(Schemas schemas, CommonROutputScript commonScript, BaseUnitWeightROutputScript weightScript,
			List<EntityPlotAreaROutputScript> plotAreaScripts, List<CalculationStepROutputScript> chainScripts, ErrorFunctionsROutputScript errorFunctionsScript ) {
		
		RScript r 				= r();
		Workspace workspace 	= schemas.getWorkspace();
		
		ProcessingChain chain 	= workspace.getDefaultProcessingChain();
		
		
		CalcPersistCommonScript persistCommonScript = r().calcPersistCommonScript( getFileNameStringVar(commonScript) , chain.getId() );
		r.addScript( persistCommonScript );
		
		if( weightScript != null ){
			SamplingDesign samplingDesign = workspace.getSamplingDesign();
			
			CalcPersistBaseUnitWeightScript persistWeightScript = r().calcPersistBaseUnitWeightScript( getFileNameStringVar(weightScript) , samplingDesign.getId() );
			r.addScript( persistWeightScript );
		}
		
		if( CollectionUtils.isNotEmpty(plotAreaScripts) ){
			for (EntityPlotAreaROutputScript plotArea : plotAreaScripts) {
				
				CalcPersistEntityPlotAreaScript persistEntityPlotAreaScript = r().calcPersistEntityPlotAreaScript( getFileNameStringVar(plotArea) , plotArea.getEntity().getId() );
				r.addScript( persistEntityPlotAreaScript );
			
			}
		}
		
		if( CollectionUtils.isNotEmpty(chainScripts) ){ 
			for (CalculationStepROutputScript stepScript : chainScripts) {
				
				CalcPersistCalculationStepScript persistCalculationStepScript = r().calcPersistCalculationStepScript( getFileNameStringVar(stepScript), stepScript.getCalculationStep().getId() );
				r.addScript( persistCalculationStepScript );
			
			}
		}
		
		if( errorFunctionsScript != null ){
			
			CalcPersistErrorScript persistErrorScript = r().calcPersistErrorScript( getFileNameStringVar(errorFunctionsScript), errorFunctionsScript.getErrorSettings().getId().intValue() );
			r.addScript( persistErrorScript );
		
		}
		
		return r;
	}

	private static RVariable getFileNameStringVar( ROutputScript script ){
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append( "'" );
		stringBuilder.append( script.getFileName() );
		stringBuilder.append( "'" );
		
		return r().variable( stringBuilder.toString() );
	}

}
