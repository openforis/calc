package org.openforis.calc.chain.export;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.metadata.SamplingDesign;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.FactTable;

/**
 * 
 * @author M. Togna
 *
 */
public class BaseUnitWeightROutputScript extends ROutputScript {

	public BaseUnitWeightROutputScript(int index , Workspace workspace) {
		super( "base-unit-weight.R", createScript(workspace), Type.USER , index );
	}

	private static RScript createScript(Workspace workspace) {
		RScript r = r();
		
		if( workspace.hasSamplingDesign() ){
			SamplingDesign samplingDesign 	= workspace.getSamplingDesign();
			String weightScript 			= samplingDesign.getSamplingUnitWeightScript();
			
			if( StringUtils.isBlank(weightScript) ){
				
				Entity entity 		= samplingDesign.getSamplingUnit();
				RVariable weightVar = r().variable( entity.getName(), FactTable.WEIGHT_COLUMN );
				SetValue setWeight 	= r().setValue( weightVar, r().rScript("1") );
				
				r.addScript( setWeight );
				
			} else {
				
				r.addScript( r().rScript( weightScript ) );
			
			}
		}
		
		return r;
	}

}
