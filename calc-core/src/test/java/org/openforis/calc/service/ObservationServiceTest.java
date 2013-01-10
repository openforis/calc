package org.openforis.calc.service;

import java.io.IOException;

import org.openforis.calc.io.csv.CsvReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ObservationServiceTest {

	@Value("${testDataPath}")
	private String testDataPath = "~/home/gino/tzdata";

	@Autowired
	private ObservationService observationService;
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/applicationContext.xml");
			ObservationServiceTest test = ctx.getBean(ObservationServiceTest.class);
			test.run();
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}

	private void run() throws IOException {
		CsvReader in = new CsvReader(testDataPath+"/trees.csv");
		in.readHeaders();
		observationService.importSpecimenData("naforma1", "tree", in);
	}
}
