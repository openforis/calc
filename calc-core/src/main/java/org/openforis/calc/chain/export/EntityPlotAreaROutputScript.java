package org.openforis.calc.chain.export;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.metadata.Entity;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.RVariable;
import org.openforis.calc.r.SetValue;
import org.openforis.calc.schema.ResultTable;

/**
 * 
 * @author M. Togna
 *
 */
public class EntityPlotAreaROutputScript extends ROutputScript {

	private static final String NAME_FORMAT = "%s-plot-area.R";
	
	public EntityPlotAreaROutputScript(int index , Entity entity ) {
		super ( String.format(NAME_FORMAT, entity.getName() ), createScript(entity), Type.USER , index );
	}

	private static RScript createScript( Entity entity ) {
		RScript r 			= r();
		Workspace workspace = entity.getWorkspace();
		
		if( workspace.hasSamplingDesign() ){
			String plotAreaScript 			= entity.getPlotAreaScript();
			
			if( StringUtils.isBlank(plotAreaScript) ){
				
				RVariable plotAreaVar 	= r().variable( entity.getName(), ResultTable.PLOT_AREA_COLUMN_NAME );
				SetValue setPlotArea 	= r().setValue( plotAreaVar, r().rScript("0.5") );
				
				r.addScript( setPlotArea );
				
			} else {
				
				r.addScript( r().rScript( plotAreaScript ) );
			
			}
		}
		
		return r;
	}

}
