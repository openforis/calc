package sandbox;

import org.openforis.calc.service.RolapService;
import org.springframework.context.support.ClassPathXmlApplicationContext;
//import mondrian.olap.MondrianDef.*;

/**
 * 
 * @author G. Miceli
 *
 */
public class MondrianApiMockup {
	public static void main(String[] args) {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			RolapService rolapService = ctx.getBean(RolapService.class);
			String mdxFileName = ctx.getBeanFactory().resolveEmbeddedValue("${mdxOutputPath}");
			//"/opt/dev/saiku-server/tomcat/webapps/saiku/WEB-INF/classes/naforma1/Naforma1.xml";
			rolapService.publishRolapSchema("naforma1", "testschema", mdxFileName);
//			printSchema();
//			testQuery();
//			manuallyCreateSchema();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

//	private static void printSchema() {
//		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//		MetadataService metadataService = ctx.getBean(MetadataService.class);
//		SurveyMetadata surveyMetadata = metadataService.getSurveyMetadata("naforma1");
//		RolapSchemaGenerator rsg = new RolapSchemaGenerator(surveyMetadata, "testschema");
//		RolapSchemaDefinition defn = rsg.generateDefinition();
//		System.out.println("SCHEMA ---------------------------------------");
//		
//		System.out.println(defn.getMondrianSchemaXml());
//		
//		System.out.println("TABLES ---------------------------------------");
//		for (RolapTable dt : defn.getDatabaseTables()) {
//			System.out.println(dt.getClass().getSimpleName()+"\t\t"+ dt.getName());
//		}
//	}
//
//	private static void manuallyCreateSchema() {
//		Schema schema = new Schema();
//		schema.name = "naforma1";
//		
//		Cube cube = new Cube();
//		cube.name = "Plot";
//		cube.visible = true;
//		cube.caption = "Plots";
//		Table fact = new Table();
//		fact.name = "plot_fact";
//		fact.schema = "naforma1";
//		AggName agg = new AggName();
//		agg.name = "agg_district_stratum_plot_fact";
//		agg.factcount = new AggFactCount();
//		agg.factcount.column = "count";
//		AggForeignKey fk1 = new AggForeignKey();
//		fk1.factColumn = "aoi_id";
//		fk1.aggColumn = "aoi_id";
//		AggForeignKey fk2 = new AggForeignKey();
//		fk2.factColumn = "stratum_id";
//		fk2.aggColumn = "stratum_id";
//		agg.foreignKeys = new AggForeignKey[] { fk1, fk2 };
//		fact.aggTables = new AggTable[] { agg };
//		cube.fact = fact;
//		schema.cubes = new Cube[] {cube};
//		schema.displayXML(new XMLOutput(new PrintWriter(System.out, true)), 1);
//	}
//
//	private static void testQuery() {
//		Connection connection = DriverManager.getConnection(
//		    "Provider=mondrian;" +
//		    "Jdbc=jdbc:hsqldb:/opt/saiku/saiku-server/tomcat/webapps/saiku/WEB-INF/classes/foodmart/foodmart;" +
//		    "Catalog=/opt/saiku/saiku-server/tomcat/webapps/saiku/WEB-INF/classes/foodmart/FoodMart.xml;",
//		    null);
//		
//		RolapSchema schema = (RolapSchema) connection.getSchema();
//		
//		Query query = connection.parseQuery("SELECT NON EMPTY {Hierarchize({[Pay Type].[Pay Type].Members})} ON COLUMNS, NON EMPTY CrossJoin([Store].[Store State].Members, {[Measures].[Avg Salary]}) ON ROWS FROM [HR]");
//		Result result = connection.execute(query);
//		result.print(new PrintWriter(System.out));
//	}
}
