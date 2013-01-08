package org.openforis.calc.io;

import java.util.List;

import org.openforis.calc.io.flat.Record;

/**
 * @author G. Miceli
 */
public interface FlatDataStream {
	List<String> getFieldNames();
	Record nextRecord();
}
