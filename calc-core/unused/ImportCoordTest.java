package sandbox;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.geotools.geometry.DirectPosition2D;
import org.openforis.calc.geospatial.GeodeticCoordinate;
import org.openforis.calc.geospatial.TransformationUtils;
import org.openforis.calc.importer.ImportException;
import org.openforis.calc.importer.PlotSectionImporter;
import org.openforis.calc.importer.SpecimenImporter;
import org.openforis.calc.model.SamplePlot;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.persistence.SamplePlotDao;
import org.openforis.calc.service.ObservationService;
import org.openforis.commons.io.csv.CsvReader;
import org.postgis.PGgeometry;
import org.postgresql.util.PGobject;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author M. Togna
 * 
 */
public class ImportCoordTest {

	private static ClassPathXmlApplicationContext appContext;

	static {
		appContext = new ClassPathXmlApplicationContext("applicationContext.xml");
	}

	public static void main(String[] args) {
		try {
//		    Map<String,Object> params = new HashMap<String,Object>();
//		    params.put( "dbtype", "postgis");
//		    params.put( "host", "exlpropforis1.ext.fao.org");
//		    params.put( "port", 54321);
//		    params.put( "schema", "calc");
//		    params.put( "database", "calc");
//		    params.put( "user", "calc");
//		    params.put( "passwd", "calc123");
//		    DataStore dataStore=DataStoreFinder.getDataStore(params);			
			
			
			SamplePlotDao dao = appContext.getBean(SamplePlotDao.class);
			SamplePlot sp = new SamplePlot();
			sp.setSamplePlotId(-1000);
			sp.setObsUnitId(2);
			sp.setPlotNo(100);
			sp.setPermanentPlot(false);
			sp.setGroundPlot(false);
			GeodeticCoordinate loc1 = GeodeticCoordinate.toInstance(173210,  9363900, "EPSG:21037");
//			System.out.printf("%f %f\n", loc1.getLongitude(), loc1.getLatitude());
			System.out.println(loc1.toString());
//			PGgeometry loc = new PGgeometry(loc1.toString());
//			PGobject loc = new PGobject() {
//				@Override
//				public String getValue() {
//					return "ST_Transform(ST_GeomFromText('POINT(173210 9363900)',21037), 4326)";
//				}
//			};
//			loc.setType("geometry");
//			PGgeometry loc = new PGgeometry("SRID=4326;POINT(36.050627550947 -5.74991181008762");
			sp.setPlotLocation(loc1);
//			sp.setPlotLocation("ST_Transform(ST_SetSRID(ST_Point(173210, 9363900),21037), 4326)::geom");
//			sp.setPlotLocation("ST_Transform(ST_GeomFromText('POINT(173210 9363900)',21037), 4326)");
//			dao.insert(sp);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}
}
