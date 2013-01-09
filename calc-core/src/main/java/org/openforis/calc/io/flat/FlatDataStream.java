package org.openforis.calc.io.flat;

import java.io.IOException;
import java.util.List;


/**
 * @author G. Miceli
 */
public interface FlatDataStream {
	
	List<String> getFieldNames();
	
	Record nextRecord() throws IOException;
	
}
