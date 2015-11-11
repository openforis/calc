package org.openforis.calc.chain.export;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.metadata.ErrorSettings;
import org.openforis.calc.r.RScript;

/**
 * 
 * @author M. Togna
 *
 */
public class ErrorFunctionsROutputScript extends ROutputScript {

	public ErrorFunctionsROutputScript( int index, ErrorSettings errorSettings ) {
		super( "error-functions.R", createScript(errorSettings), Type.USER, index );
	}

	private static RScript createScript( ErrorSettings errorSettings ) {
		RScript r = r();

		String errorScript = errorSettings.getScript();
		if (StringUtils.isNotBlank(errorScript)) {
			r.addScript(r().rScript(errorScript));
		}

		return r;
	}

}
