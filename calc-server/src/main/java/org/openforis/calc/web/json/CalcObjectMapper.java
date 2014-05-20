/**
 * 
 */
package org.openforis.calc.web.json;

import org.openforis.calc.engine.ParameterHashMap;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Mino Togna
 * @author S. Ricci
 */
public class CalcObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;

	public CalcObjectMapper() {
		SimpleModule module = new SimpleModule( "CalcSimpleModule", new Version(1, 0, 0, "alpha","org.openforis.calc","calc-webapp") );
	    module.addSerializer( ParameterHashMap.class, new ParameterMapJsonSerializer());
	    this.registerModule(module);
	}
	
}
