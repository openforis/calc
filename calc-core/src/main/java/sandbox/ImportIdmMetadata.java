package sandbox;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.openforis.calc.importer.collect.HumanImpactMetadataImporter;
import org.openforis.calc.importer.collect.IdmMetadataImporter;
import org.openforis.calc.importer.collect.InvalidMetadataException;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author G. Miceli
 */
public class ImportIdmMetadata {
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			
			importHumanImpactMetadata(ctx);
			
//			importMetadata(ctx);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	static void importHumanImpactMetadata(ClassPathXmlApplicationContext ctx) throws FileNotFoundException, IOException, IdmlParseException {
		HumanImpactMetadataImporter loader = ctx.getBean(HumanImpactMetadataImporter.class);
		FileReader in = new FileReader("/home/minotogna/dev/projects/faofin/tz/naforma-idm/tanzania-naforma.idm.3.0-CR1.xml");
		loader.importMetadata("test", in);
		in.close();
	}

	static void importMetadata(ClassPathXmlApplicationContext ctx) throws FileNotFoundException, IOException, IdmlParseException, InvalidMetadataException {
		IdmMetadataImporter loader = ctx.getBean(IdmMetadataImporter.class);
		FileReader in = new FileReader("/home/gino/tzdata/idml.xml");
		loader.importMetadata("test", in);
	}
}
