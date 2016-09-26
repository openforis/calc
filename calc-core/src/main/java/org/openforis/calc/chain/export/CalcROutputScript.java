/**
 * 
 */
package org.openforis.calc.chain.export;

import org.openforis.calc.chain.CalculationStep;
import org.openforis.calc.r.RScript;
import org.openforis.calc.r.Source;

/**
 * @author M. Togna
 *
 */
public class CalcROutputScript extends ROutputScript {

	private boolean standAloneMode;

	/**
	 */
	public CalcROutputScript(boolean standAloneMode) {
		super("calc.R", new RScript());
		this.standAloneMode = standAloneMode;

		// addScript( r().setWd(r().rScript(".")));
	}

	private void addScript(RScript script) {
		super.getRScript().addScript(script);
	}

	public void addScript(ROutputScript script) {
		Source source = r().source(script.getFileName());
		RScript rScript = source;
		if (script instanceof CalculationStepROutputScript) {
			CalculationStep step = ((CalculationStepROutputScript) script).getCalculationStep();
			if (!step.getActive()) {
				rScript = r().comment(source);
			}
		}
		addScript(rScript);

		if (standAloneMode) {
			addScript(r().rScript("cat('==========" + script.getIndex() + "\\n');"));
		}

	}

	@Override
	public RScript getRScript() {
		RScript script = null;
		if (standAloneMode) {
			RScript chainScript = new RScript();
			chainScript.addScript(r().rScript("f <- file(\"output.txt\");"));
			chainScript.addScript(r().rScript("sink(f, append=TRUE);"));
			chainScript.addScript(r().rScript("sink(f, append=TRUE, type=\"message\");"));
			chainScript.addScript(r().rScript("cat('==========0\\n');"));
			chainScript.addScript(super.getRScript());

			RScript errorScript = new RScript();
			errorScript.addScript(r().rScript("cat('==========-1\\n');\n calc.error('-',e);"));
			errorScript.addScript(r().rScript("dbDisconnect(connection);"));
			
			script = r().rTryCatch(chainScript, errorScript, null);
		} else {
			script = super.getRScript();
		}
		
		return script;
	}

}
