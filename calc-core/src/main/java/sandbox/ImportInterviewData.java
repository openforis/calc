package sandbox;

import org.openforis.calc.importer.InterviewImporter;
import org.openforis.calc.io.csv.CsvReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 */
public class ImportInterviewData {

	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			InterviewImporter imp = ctx.getBean(InterviewImporter.class);
			imp.setSurveyName("naforma1");
			imp.setObservationUnitName("household");
			CsvReader csv = new CsvReader("/home/gino/tzdata/household.csv");
			csv.readHeaders();
			imp.importData(csv);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

}
