package sandbox;

import org.openforis.calc.importer.SpecimenImporter;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author M. Togna
 *
 */
public class ImportSpecimenData {

	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			
			SpecimenImporter importer = ctx.getBean(SpecimenImporter.class);
			importer.setSurveyName("naforma1");
			importer.setObservationUnitName("tree");
			
			CsvReader csv = new CsvReader("/home/minotogna/tzdata/trees.csv");
			csv.readHeaders();
			importer.importData(csv);
			
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

}
