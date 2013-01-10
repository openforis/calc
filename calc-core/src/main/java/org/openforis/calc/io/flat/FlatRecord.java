package org.openforis.calc.io.flat;

import java.util.Date;
import java.util.List;

/**
 * @author G. Miceli
 */
public interface FlatRecord {

	Integer getInteger(String name);

	Double getDouble(String name);

	Boolean getBoolean(String name);

	String getString(String name);

	Date getDate(String name);

	Integer getInteger(int idx);

	Double getDouble(int idx);

	Boolean getBoolean(int idx);

	Date getDate(int idx);

	String[] toStringArray();

	FlatDataStream getFlatDataStream();

	List<String> getFieldNames();

	<T> T getValue(int idx, Class<T> type);
	
	<T> T getValue(String name, Class<T> type);

}
