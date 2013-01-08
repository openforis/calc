package org.openforis.calc.io.flat;

/**
 * @author G. Miceli
 */
public interface Record {
	Integer getInteger(String name);
	Double getDouble(String name);
	Boolean getBoolean(String name);
	String getString(String name);
	String[] toStringArray();
}
