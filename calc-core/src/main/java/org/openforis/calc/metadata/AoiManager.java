/**
 * 
 */
package org.openforis.calc.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.openforis.calc.engine.Workspace;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.stereotype.Component;

/**
 * @author Mino Togna
 *
 */
@Component
public class AoiManager {

	public void csvImport(Workspace workspace, String filepath, String[] levels) throws IOException{
		CsvReader csvReader = new CsvReader(filepath);
		csvReader.readHeaders();
		
		List<AoiHierarchy> aoiHierarchies = workspace.getAoiHierarchies();
		
		FlatRecord record = null;
		do{
			record = csvReader.nextRecord();
			
		} while (record != null);
	}
	
}
