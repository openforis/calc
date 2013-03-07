/**
 * 
 */
package org.openforis.calc.importer;

import java.util.List;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.model.SpecimenCategoricalValue;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenCategoricalValueDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenNumericValueDao;
import org.openforis.calc.persistence.TaxonDao;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 * 
 */
@Component
public class SpecimenImporter extends AbstractObservationImporter<Specimen, SpecimenNumericValue, SpecimenCategoricalValue> {

	@Autowired
	private SpecimenDao specimenDao;
	@Autowired
	private SpecimenNumericValueDao specimenNumericValueDao;
	@Autowired
	private SpecimenCategoricalValueDao specimenCategoricalValueDao;
	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private TaxonDao taxonDao;

	private ObservationUnitMetadata obsUnitParent;

	public SpecimenImporter() {
		super(SpecimenNumericValue.class, SpecimenCategoricalValue.class);
		setInsertFrequency(2000);
		setReportFrequency(2000);
	}

	@Override
	protected Specimen processObservation(FlatRecord record) {
		String clusterCode = record.getValue("cluster_code", String.class);
		String visitType = record.getValue("visit_type", String.class);
		Integer plotNo = record.getValue("plot_no", Integer.class);
		String plotSection = record.getValue("plot_section", String.class);
		String taxonCode = record.getValue("taxon_code", String.class);
		Integer specimenNo = record.getValue("specimen_no", Integer.class);

		Integer plotSectionId = getPlotSectionId(clusterCode, plotNo, visitType, plotSection);

		if ( plotSectionId == null ) {
			String plotKey = clusterCode + "_" + plotNo + "_" + plotSection + "_" + visitType;
			log.warn("Plot Section id not found: " + plotKey);
			return null;
		}

		Integer obsUnitId = getObservationUnitMetadata().getObsUnitId();
		Integer specimenId = specimenDao.nextId();
		Integer taxonId = taxonDao.getIdByKey(taxonCode);

		Specimen specimen = new Specimen();
		specimen.setId(specimenId);
		specimen.setPlotSectionId(plotSectionId);
		specimen.setObsUnitId(obsUnitId);
		specimen.setSpecimenNo(specimenNo);
		specimen.setSpecimenTaxonId(taxonId);

		return specimen;
	}

	@Override
	protected void doInserts(List<Specimen> obs, List<SpecimenNumericValue> numVals, List<SpecimenCategoricalValue> catVals) {
		specimenDao.insert(obs);
		specimenNumericValueDao.insert(numVals);
		specimenCategoricalValueDao.insert(catVals);
	}

	private Integer getPlotSectionId(String clusterCode, Integer plotNo, String visitType, String plotSection) {
		if ( clusterCode == null || visitType == null || plotNo == null || plotSection == null ) {
			return null;
		} else {
			// V.PLOT_OBS_UNIT_ID, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE)
			Integer id = plotSectionViewDao.getIdByKey(obsUnitParent.getObsUnitId(), clusterCode.trim(), plotNo, plotSection.trim(), visitType.trim());
			return id;
		}
	}

	@Override
	protected void onStart() {
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		obsUnitParent = unit.getObsUnitParent();
	}

}
