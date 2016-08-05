/**
 * 
 */
package org.openforis.calc.web.controller;

import static org.openforis.calc.system.SystemProperty.PROPERTIES.R_EXEC_DIR;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.Calc;
import org.openforis.calc.system.SystemProperty;
import org.openforis.calc.system.SystemPropertyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author M. Togna
 * @author S. Ricci
 *
 */
@Controller
@RequestMapping(value = "/rest/calc")
public class CalcController {

	@Autowired
	private Calc calc;

	@Autowired
	private SystemPropertyManager systemPropertyManager;

	@RequestMapping(value = "/info.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Calc getCalcInfo() {
		return calc;
	}

	@RequestMapping(value = "/system-properties.json", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<SystemProperty> getSystemProperties() {
		return systemPropertyManager.getAll();
	}

	@RequestMapping(value = "/system-properties/save.json", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Response saveSystemProperties(@RequestParam("r_exec_dir") String rExecDir) {
		Response response = new Response();

		if (validateRDir(response, rExecDir)) {
			systemPropertyManager.save(R_EXEC_DIR.toString(), rExecDir);
		}

		return response;
	}

	private boolean validateRDir(Response response, String rExecDir) {
		String error = null;
		if ( StringUtils.isBlank(rExecDir) ) {
			error = " is invalid directory";
		} else {
			File f = new File(rExecDir);
			if (!(f.exists() && f.isDirectory())) {
				error = "is invalid directory";
			} else {
				File[] rFiles = f.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						if (name.equals("Rscript.exe") || name.equals("Rscript.sh") || name.equals("Rscript")) {
							return true;
						}
						return false;
					}
				});

				if (rFiles.length <= 0) {
					error = "must contain valid Rscript executable file";
				}
			}
		}

		boolean valid = true;
		if (error != null) {
			FieldError fieldError = new FieldError("", R_EXEC_DIR.toString(), error);
			response.addError(fieldError);
			response.setStatusError();
			valid = false;
		}

		return valid;
	}
}
