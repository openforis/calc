package org.openforis.calc.nls;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides Native Language Support as defined using Java's standard
 * {@link ResourceBundle} mechanism.
 * 
 * @author G. Miceli
 */
public class NlsService {
	public String getCaption(Class<? extends Captionable> clazz, Locale locale) {
		throw new UnsupportedOperationException();
	}
	
	public String getCaption(Class<? extends Captionable> clazz) {
		throw new UnsupportedOperationException();
	}

	public String getDescription(Class<? extends Captionable> clazz, Locale locale) {
		throw new UnsupportedOperationException();
	}
	
	public String getDescription(Class<? extends Captionable> clazz) {
		throw new UnsupportedOperationException();
	}
}
