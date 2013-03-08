package sandbox;

import java.io.FileReader;

import org.openforis.calc.importer.collect.IdmMetadataImporter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author G. Miceli
 */
public class ImportIdmMetadata {
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			IdmMetadataImporter loader = ctx.getBean(IdmMetadataImporter.class);
			FileReader in = new FileReader("/home/gino/tzdata/idml.xml");
			loader.importMetadata("test", in);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}
}
