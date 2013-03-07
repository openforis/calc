/**
 * 
 */
package org.openforis.calc.importer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.PlotSectionView;
import org.openforis.calc.model.Specimen;
import org.openforis.calc.model.SpecimenCategoricalValue;
import org.openforis.calc.model.SpecimenNumericValue;
import org.openforis.calc.model.Taxon;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.calc.persistence.SpecimenCategoricalValueDao;
import org.openforis.calc.persistence.SpecimenDao;
import org.openforis.calc.persistence.SpecimenNumericValueDao;
import org.openforis.calc.persistence.TaxonDao;
import org.openforis.calc.persistence.jooq.Tables;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 * 
 */
@Component
public class SpecimenImporter extends AbstractObservationImporter<Specimen, SpecimenNumericValue, SpecimenCategoricalValue> {

	private Map<String, Integer> plotSectionIds;
	private Map<String, Integer> taxaIds;
	
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

	public SpecimenImporter() {
		super(SpecimenNumericValue.class, SpecimenCategoricalValue.class);
		setInsertFrequency(1000);
		setReportFrequency(1000);
	}

	@Override
	protected Specimen processObservation(FlatRecord record) {
		String clusterCode = record.getValue("cluster_code", String.class);
		String visitType = record.getValue("visit_type", String.class);
		String plotNo = record.getValue("plot_no", String.class);
		String plotSection = record.getValue("plot_section", String.class);
		String taxonCode = record.getValue("taxon_code", String.class);
		Integer specimenNo = record.getValue("specimen_no", Integer.class);
		
		String plotKey = getPlotSectionKey(clusterCode, plotNo, visitType, plotSection);
		Integer plotSectionId = plotSectionIds.get(plotKey);
			
		if ( plotKey == null ) {
			log.warn("Invalid plot Section: " + plotKey);
			return null;
		}
		if( plotSectionId == null ){
			log.warn("Plot Section id not found: " + plotKey);
			return null;
		}
		
		Integer obsUnitId = getObservationUnitMetadata().getObsUnitId();
		Integer specimenId = specimenDao.nextId();
		Integer taxonId = taxaIds.get(taxonCode);

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

	@Override
	protected void onStart() {
		initPlotSectionIds();
		initTaxaIds();
	}

	private void initTaxaIds() {
		taxaIds = new HashMap<String, Integer>();
		List<Taxon> taxa = taxonDao.findAll();
		for ( Taxon taxon : taxa ) {
			String taxonCode = taxon.getTaxonCode();
			Integer taxonId = taxon.getTaxonId();
			taxaIds.put(taxonCode, taxonId);
		}
	}

	private void initPlotSectionIds() {
		plotSectionIds = new HashMap<String, Integer>();

		ObservationUnitMetadata unit = getObservationUnitMetadata();
		Integer parentId = unit.getObsUnitParentId();

		List<PlotSectionView> plots = plotSectionViewDao.fetch(Tables.PLOT_SECTION_VIEW.PLOT_OBS_UNIT_ID, parentId);
		for ( PlotSectionView plot : plots ) {

			String clusterCode = plot.getClusterCode();
			Integer plotNo = plot.getPlotNo();
			String visitType = plot.getVisitType();
			String plotSection = plot.getPlotSection();

			String plotSectionKey = getPlotSectionKey(clusterCode, String.valueOf(plotNo), visitType, plotSection);
			Integer plotSectionId = plot.getPlotSectionId();
			if ( plotSectionIds.containsKey(plotSectionKey) ) {
				throw new RuntimeException("Duplicate plot key " + plotSectionKey);
			}
			plotSectionIds.put(plotSectionKey, plotSectionId);
		}
	}

	private String getPlotSectionKey(String clusterCode, String plotNo, String visitType, String plotSection) {
		if ( clusterCode == null || visitType == null || plotNo == null || plotSection == null ) {
			return null;
		} else {
			String key = clusterCode.trim() + "_" + plotNo.trim() + "_" + visitType.trim() + "_" + plotSection.trim();
			return key;
		}
	}

}
