/**
 * 
 */
package org.openforis.calc.engine;

import org.openforis.calc.metadata.Aoi;
import org.openforis.calc.metadata.CategoricalVariable;
import org.openforis.calc.metadata.QuantitativeVariable;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RScript;
import org.openforis.calc.schema.Schemas;

/**
 * @author Mino Togna
 *
 */
public class CalculateErrorTask extends CalcRTask {

	
	private QuantitativeVariable quantitativeVariable;
	private Aoi aoi;
	private CategoricalVariable<?> categoricalVariable;
	private Schemas schemas;

	protected CalculateErrorTask( REnvironment rEnvironment , QuantitativeVariable quantitativeVariable , Aoi aoi , CategoricalVariable<?> categoricalVariable, Schemas schemas ){
		super( rEnvironment , getName(quantitativeVariable, aoi, categoricalVariable) );
		
		this.quantitativeVariable = quantitativeVariable;
		this.aoi = aoi;
		this.categoricalVariable = categoricalVariable;
		this.schemas = schemas;
		
		initScript();
	}
	
	private void initScript() {
		String name = getName( quantitativeVariable, aoi, categoricalVariable );

		addScript( r().rScript("# ==========" ) );
		addScript( r().rScript("# " + name ) );
		addScript( r().rScript("# ==========" ) );
		
		RScript rScript = r().rScript( "print('"+name+"')" );
		addScript( rScript );
	}

	private static String getName( QuantitativeVariable quantitativeVariable , Aoi aoi, CategoricalVariable<?> categoricalVariable ){
		StringBuilder taskName = new StringBuilder();
		
		taskName.append( "Calculate error for " );
		taskName.append( quantitativeVariable.getName() );
		taskName.append( " " );
		taskName.append( aoi.getCaption() );
		taskName.append( " " );
		taskName.append( categoricalVariable.getName() );
		
		return taskName.toString();
	}

}
