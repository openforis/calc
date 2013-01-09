package org.openforis.calc.service;

import java.io.IOException;

import org.openforis.calc.io.csv.CsvReader;
import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.io.flat.Record;
import org.openforis.calc.model.ObservationUnit;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.persistence.SpecimenCategoryDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenMeasurementDao;
import org.openforis.calc.persistence.jooq.tables.SamplePlotView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 
 * @author G. Miceli
 *
 */
@Component
public class ObservationService {

	@Autowired 
	private MetadataService metadataService;
	@Autowired
	private PlotSectionDao plotSectionDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenMeasurementDao specimenMeasurementDao;
	@Autowired
	private SpecimenCategoryDao specimenCategoryDao;

	public static void main(String[] args)  {
		try {
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
			ObservationService svc = ctx.getBean(ObservationService.class);
			CsvReader in = new CsvReader("/home/gino/workspace/tzdata/trees.csv");
			in.readHeaders();
			svc.importSpecimenData("naforma1", "tree", in);
		} catch ( Throwable ex ) {
			ex.printStackTrace();
		}
	}
	
	public void importSpecimenData(String surveyName, String observationUnit, FlatDataStream in) throws IOException {
		ObservationUnit unit = metadataService.getObservationUnit(surveyName, observationUnit);
		if ( unit == null ) {
			throw new IllegalArgumentException("Invalid survey or observation unit");
		}
		if ( !"specimen".equals(unit.getType()) ) {
			throw new IllegalArgumentException("Invalid observation unit type: "+unit.getType()); 
		}
		Record r;
		while ( (r = in.nextRecord()) != null ) {
			System.out.println(r.getString(SamplePlotView.SAMPLE_PLOT_VIEW.CLUSTER_CODE.getName()));
		}
	}
}
