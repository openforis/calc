package org.openforis.calc.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openforis.calc.r.R;
import org.openforis.calc.r.REnvironment;
import org.openforis.calc.r.RException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping(value = "/rest/r")
public class RController {

	@Autowired
	private R r;
	
	/**
	 * Cache of R functions
	 */
	private List<String> rFunctions = null;
	
	@RequestMapping(value = "/functions.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	synchronized List<String> getRFunctions(@RequestParam(required=false) final String startsWith) throws RException {
		if ( rFunctions == null ) {
			rFunctions = loadRFunctions();
		}
		if ( startsWith == null ) {
			return rFunctions;
		} else {
			List<String> result = new ArrayList<String>();
			for (String funct : rFunctions) {
				if ( funct.startsWith(startsWith) ) {
					result.add(funct);
				}
			}
			return result;
		}
	}

	private List<String> loadRFunctions() throws RException {
		REnvironment rEnvironment = r.newEnvironment();
		String[] result = rEnvironment.evalStrings("as.vector( lsf.str(\"package:base\") )");
		return Arrays.asList(result);
	}
}
