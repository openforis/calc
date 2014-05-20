/**
 * 
 */
package org.openforis.calc.web.controller;

import org.openforis.calc.Calc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping(value = "/rest/calc")
public class CalcController {
	
	@Autowired
	private Calc calc;
	
	@RequestMapping(value = "/info.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Calc getCalcInfo() {
		return calc;
	}
	
}
