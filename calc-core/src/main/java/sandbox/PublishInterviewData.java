package sandbox;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author G. Miceli
 *
 */
public class PublishInterviewData {

	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//			InterviewImporter imp = ctx.getBean(InterviewImporter.class);
//			imp.setSurveyName("naforma1");
//			imp.setObservationUnitName("household");
//			CsvReader csv = new CsvReader("/home/gino/tzdata/household.csv");
//			csv.readHeaders();
//			imp.importData(csv);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

}
