/**
 * 
 */
package org.openforis.calc.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;
import org.renjin.parser.RParser;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.sexp.ExpressionVector;

/**
 * @author Mino Togna
 * 
 */
public abstract class AbstractROperation implements Operation {

	private static final String FILE_EXTENSION = ".R";
	private String script;
	private RenjinScriptEngine renjinScriptEngine;
	private ExpressionVector expressionVector;

	public AbstractROperation() {
	}

	@PostConstruct
	protected void init() {
		String fileName = this.getClass().getName().replaceAll("\\.", "/");
		fileName += FILE_EXTENSION;
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
			StringWriter writer = new StringWriter();
			IOUtils.copy(stream, writer);
			script = writer.toString();
			stream.close();
			
			expressionVector = RParser.parseSource(getScript() + "\n");
			ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			renjinScriptEngine = (RenjinScriptEngine) scriptEngineManager.getEngineByName("Renjin");
		} catch ( IOException e ) {
			throw new RuntimeException("Error while creating Operation", e);
		}
	}

	public String getScript() {
		return script;
	}	
	
	protected ExpressionVector getExpressionVector() {
		return expressionVector;
	}

	protected RenjinScriptEngine getRenjinScriptEngine() {
		return renjinScriptEngine;
	}

}
