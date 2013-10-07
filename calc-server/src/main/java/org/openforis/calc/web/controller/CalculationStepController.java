/**
 * 
 */
package org.openforis.calc.web.controller;

import javax.validation.Valid;

import org.openforis.calc.web.form.CalculationStepForm;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author S. Ricci
 * 
 */
@Controller
@RequestMapping(value = "/rest/calculationstep")
public class CalculationStepController {

	@RequestMapping(value = "/save.json", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Response save(@Valid CalculationStepForm form, BindingResult result) {
		Response response = new Response(result.getAllErrors());
		
		if (!response.hasErrors()) {
			// save the calculation step
		}
//<a href="#" data-toggle="tooltip" data-placement="top" title="" data-original-title="Tooltip on fuck">Tooltip on top</a>
		return response;
	}
}
