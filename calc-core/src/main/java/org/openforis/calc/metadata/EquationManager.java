/**
 * 
 */
package org.openforis.calc.metadata;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openforis.calc.engine.Workspace;
import org.openforis.calc.persistence.jooq.tables.daos.EquationDao;
import org.openforis.commons.io.csv.CsvReader;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Mino Togna
 *
 */
@Component
public class EquationManager {
	
	@Autowired
	private EquationListDao equationListDao;
	
	@Autowired
	private EquationDao equationDao;
	
	@Transactional
	public void importFromCsv( Workspace workspace , String filePath, String listName ) throws IOException {
		CsvReader csvReader = new CsvReader(filePath);
		csvReader.readHeaders();
		
		for (FlatRecord record = csvReader.nextRecord(); record != null; record = csvReader.nextRecord()) {
			System.out.println( record );
		}
		
		csvReader.close();
	}
	
}
