package sandbox;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.openforis.calc.importer.ImportException;
import org.openforis.calc.importer.PlotSectionImporter;
import org.openforis.calc.importer.SpecimenImporter;
import org.openforis.calc.service.ObservationService;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author M. Togna
 * 
 */
public class ImportObservations {

	private static final String DATA_FOLDER = "/home/minotogna/tzdata";
	private static final String TREES_CSV_FILENAME = DATA_FOLDER + "/trees.csv";
	private static final String DEAD_WOOD_CSV_FILENAME = DATA_FOLDER + "/dead_wood.csv";
	private static final String PLOT_CSV_FILENAME = DATA_FOLDER + "/plots.csv";
	private static ClassPathXmlApplicationContext appContext;

	static {
		appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
	}

	public static void main(String[] args) {
		try {
			String survey = "naforma1";

			
//			removeData( survey, "tree" );
//			removeData( survey, "dead_wood" );
//			removeData( survey, "plot" );

			// import
//			importPlots(survey, "plot", PLOT_CSV_FILENAME);
			 importSpecimens( survey, "tree" , TREES_CSV_FILENAME);
//			 importSpecimens( survey, "dead_wood" , DEAD_WOOD_CSV_FILENAME);

		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	static void removeData(String survey, String observationUnit) {
		ObservationService obsService = appContext.getBean(ObservationService.class);
		obsService.removeData(survey, observationUnit);
	}

	static void importSpecimens(String survey, String observationUnit, String filename) throws FileNotFoundException, IOException, ImportException {

		SpecimenImporter importer = appContext.getBean(SpecimenImporter.class);
		importer.setSurveyName(survey);
		importer.setObservationUnitName(observationUnit);

		CsvReader csv = new CsvReader(filename);
		csv.readHeaders();
		importer.importData(csv);
	}

	private static void importPlots(String survey, String observationUnit, String filename) throws FileNotFoundException, IOException, ImportException {

		PlotSectionImporter importer = appContext.getBean(PlotSectionImporter.class);

		importer.setSurveyName(survey);
		importer.setObservationUnitName(observationUnit);

		CsvReader csv = new CsvReader(filename);
		csv.readHeaders();
		importer.importData(csv);
	}

}
