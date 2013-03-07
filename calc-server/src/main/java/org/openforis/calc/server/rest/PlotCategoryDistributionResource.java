package org.openforis.calc.server.rest;

import javax.ws.rs.GET;

import org.openforis.calc.service.ObservationService;
import org.openforis.calc.service.ObservationService.PlotDistributionCalculationMethod;
import org.openforis.commons.io.flat.FlatDataStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 
 * @author M. Togna
 * 
 */
@Component
@Scope("request")
@Lazy
public class PlotCategoryDistributionResource extends SubResource<Void> {

	@Autowired
	private SurveyResource surveyResource;
	
	@Autowired
	private ObservationUnitResource observationUnitResource;
	
	@Autowired
	private ObservationService observationService;
	
	@GET
	public FlatDataStream getDistribution() {
		String surveyName = surveyResource.getKey();
		String observationUnitName = observationUnitResource.getKey();
		
		return observationService.getPlotCategoryDistributionStream(surveyName , observationUnitName, PlotDistributionCalculationMethod.PRIMARY_SECTION_ONLY );
	}

}
