package org.openforis.calc.chain.export;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.chain.ProcessingChain;
import org.openforis.calc.engine.Workspace;
import org.openforis.calc.r.RScript;

/**
 * 
 * @author M. Togna
 *
 */
public class CommonROutputScript extends ROutputScript {

	public CommonROutputScript(int index , Workspace workspace) {
		super( "common.R", createScript(workspace), Type.USER , index );
	}

	private static RScript createScript(Workspace workspace) {
		RScript r = r();
		
		ProcessingChain chain 	= workspace.getDefaultProcessingChain();
		String commonScript 	= chain.getCommonScript();
		if( StringUtils.isNotBlank(commonScript) ){
			r.addScript( r().rScript(commonScript) );
		}
		
		return r;
	}

}
