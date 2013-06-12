package sandbox;

import org.openforis.calc.importer.PlotSectionMultipleCategoricalBooleanValueImporter;
import org.openforis.commons.io.csv.CsvReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author M. Togna
 */
public class ImportHumanImpactData {
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			String testDataPath = ctx.getBeanFactory().resolveEmbeddedValue("${testDataPath}");
			String filename = testDataPath+"/human_impact.csv";
			CsvReader csv = new CsvReader(filename);
			csv.readHeaders();

			PlotSectionMultipleCategoricalBooleanValueImporter importer = ctx.getBean(PlotSectionMultipleCategoricalBooleanValueImporter.class);
			importer.setSurveyName("naforma1");
			importer.setObservationUnitName("plot");
			
			importer.importData(csv);
			
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	
}
