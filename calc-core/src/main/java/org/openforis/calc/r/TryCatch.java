/**
 * 
 */
package org.openforis.calc.r;

/**
 * @author Mino Togna
 *
 */
public class TryCatch extends RScript {

	TryCatch(RScript previous, RScript script, RScript errorScript, RScript finallyScript) {
		super(previous);
		append("tryCatch({");
		append(RScript.NEW_LINE);
		append(script.toString());
		append(RScript.NEW_LINE);
		append("}");

		if (errorScript != null) {
			append(RScript.NEW_LINE);
			append(", error = function(e){");
			append(RScript.NEW_LINE);
			append(errorScript.toString());
			append(RScript.NEW_LINE);
			append("}");
		}

		if (finallyScript != null) {
			append(RScript.NEW_LINE);
			append(", finally = {");
			append(RScript.NEW_LINE);
			append(finallyScript.toString());
			append(RScript.NEW_LINE);
			append("}");
		}

		append(")");
	}

}
