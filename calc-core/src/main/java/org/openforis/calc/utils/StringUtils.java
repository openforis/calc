/**
 * 
 */
package org.openforis.calc.utils;

import java.text.Normalizer;

/**
 * @author Mino Togna
 *
 */
public class StringUtils {
	
	public static String normalize( String string ){
		String value = Normalizer.normalize( string, Normalizer.Form.NFD );
		value = value.replaceAll("\\W", "_").toLowerCase();
		value = value.replaceAll( "_+", "_");
		return value;
	}
	
}
