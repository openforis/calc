package org.openforis.calc.service;

import org.openforis.calc.io.flat.FlatDataStream;
import org.openforis.calc.model.SpecimenCategory;
import org.openforis.calc.model.SpecimenMeasurement;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.springframework.beans.factory.annotation.Autowired;
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
	private SamplingDesignService samplingDesignService;
	@Autowired
	private PlotSectionDao plotSectionDao;
	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenMeasurement specimenMeasurementDao;
	@Autowired
	private SpecimenCategory specimenCategoryDao;

	public void importSpecimenData(FlatDataStream in) {
		
	}
}
