/**
 * 
 */
package org.openforis.calc.utils;

import java.text.Normalizer;

import org.apache.commons.lang3.text.WordUtils;

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
	
	public String capitalize( String string ){
		return WordUtils.capitalize(string);
	}
	
}
