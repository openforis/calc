package sandbox;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openforis.calc.importer.ImportException;
import org.openforis.calc.importer.SpecimenImporter;
import org.openforis.calc.service.ObservationService;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author M. Togna
 * 
 */
public class ImportSpecimenData {

	private static final String TREES_CSV_FILENAME = "/home/minotogna/tzdata/trees.csv";
	private static final String DEAD_WOOD_CSV_FILENAME = "/home/minotogna/tzdata/dead_wood.csv";
	private static ClassPathXmlApplicationContext appContext;
	
	static {
		appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	
	public static void main(String[] args) {
		try {
			String survey = "naforma1";
			
//			String observationUnit = "tree";
//			String fileName = TREES_CSV_FILENAME;
			
			String observationUnit = "dead_wood";
			String fileName = DEAD_WOOD_CSV_FILENAME;
			
			removeData( survey, observationUnit );
			importData( survey, observationUnit , fileName);
			
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	private static void removeData(String survey, String observationUnit) {
		ObservationService obsService = appContext.getBean( ObservationService.class );
		obsService.removeData(survey, observationUnit);
	}

	private static void importData(String survey, String observationUnit, String filename) throws FileNotFoundException, IOException, ImportException {
		
		SpecimenImporter importer = appContext.getBean(SpecimenImporter.class);
		importer.setSurveyName(survey);		
		importer.setObservationUnitName(observationUnit);

		CsvReader csv = new CsvReader(filename);
		csv.readHeaders();
		importer.importData(csv);
	}

}
