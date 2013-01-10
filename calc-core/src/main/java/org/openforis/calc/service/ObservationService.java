package org.openforis.calc.service;

import java.io.IOException;

import org.jooq.exception.DataAccessException;
import org.openforis.calc.io.csv.CsvReader;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.FlatRecord;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenCategoricalValueDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenNumericValueDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class ObservationService extends CalcService {

	@Autowired 
	private MetadataService metadataService;
	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenNumericValueDao specimenMeasurementDao;
	@Autowired
	private SpecimenCategoricalValueDao specimenCategoryDao;

	@Value("${testDataPath}")
	private String testDataPath;
	
	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			ObservationService svc = ctx.getBean(ObservationService.class);
			svc.test();
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}
	
	private void test() throws IOException {
		CsvReader in = new CsvReader(testDataPath+"/trees.csv");
		in.readHeaders();
		importSpecimenData("naforma1", "tree", in);
	}

	public void importSpecimenData(String surveyName, String observationUnit, FlatDataStream in) throws IOException {
		ObservationUnit specimenUnit = metadataService.getObservationUnit(surveyName, observationUnit);
		if ( specimenUnit == null ) {
			throw new IllegalArgumentException("Invalid survey or observation unit");
		}
		if ( !"specimen".equals(specimenUnit.getObsUnitType()) ) {
			throw new IllegalArgumentException("Invalid observation unit type: "+specimenUnit.getObsUnitType()); 
		}
		ObservationUnit plotUnit = metadataService.getObservationUnit(surveyName, "plot");		// TODO <<< implement getParent
		int plotUnitId = plotUnit.getId();
		int specimenUnitId = specimenUnit.getId();
		FlatRecord r;
		while ( (r = in.nextRecord()) != null ) {
			Object[] key = plotSectionViewDao.extractKey(r, plotUnitId);
			Integer plotSectionId = plotSectionViewDao.getIdByKey(key);
			if ( plotSectionId == null ) {
				log().warn("Skipping specimen with unknown plot "+key);
				continue;
			} 
			if ( !specimenDao.isValid(r) ) {
				log().warn("Skipping invalid record: "+r);
				continue;
			}
			try {
				Integer specimenId = specimenDao.insert(plotSectionId, specimenUnitId, r);
//				System.out.println(specimenId);
			} catch (DataAccessException e) {
				log().warn("Skipping record "+r+": "+e.getMessage());
			}
		}
	}
}
