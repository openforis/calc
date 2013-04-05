/**
 * 
 */
package org.openforis.calc.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openforis.calc.model.Category;
import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.PlotCategoricalValue;
import org.openforis.calc.model.PlotNumericValue;
import org.openforis.calc.model.PlotSection;
import org.openforis.calc.model.VariableMetadata;
import org.openforis.calc.persistence.PlotCategoricalValueDao;
import org.openforis.calc.persistence.PlotSectionDao;
import org.openforis.calc.persistence.PlotSectionViewDao;
import org.openforis.commons.io.flat.FlatRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author M. Togna
 * 
 */
@Component
public class PlotSectionMultipleCategoricalBooleanValueImporter extends AbstractObservationImporter<PlotSection, PlotNumericValue, PlotCategoricalValue> {

	@Autowired
	private PlotSectionViewDao plotSectionViewDao;
	@Autowired
	private PlotSectionDao plotSectionDao;
	@Autowired	
	private PlotCategoricalValueDao plotCategoricalValueDao;
	
	private List<VariableMetadata> variables;
	
	private Map<PlotSection, PlotSectionValue> values;
	
	public PlotSectionMultipleCategoricalBooleanValueImporter() {
		super(PlotNumericValue.class, PlotCategoricalValue.class);
	}

	@Override
	protected boolean processRecord(FlatRecord record) {
		PlotSection obs = processObservation(record);
		if ( obs != null ) {
			// observations.add(obs);
			// processValues(record, obs);
			
			String humanImpact = record.getValue( "human_impact", String.class );
			if( StringUtils.isNotBlank(humanImpact) ) {
				VariableMetadata var = findVariableByCode(humanImpact);
				if( var != null ){
					PlotSectionValue value = getValue( obs );
					value.setValue(var, humanImpact);
				}
			}
			
		}
		return true;
	}

	private PlotSectionValue getValue(PlotSection obs) {
		PlotSectionValue value = values.get(obs);
		if( value == null ){
			value = new PlotSectionValue(obs, variables);
			values.put(obs, value);
		}
		return value;
	}

	@Override
	protected PlotSection processObservation(FlatRecord record) {
		String clusterCode = record.getValue("cluster_code", String.class);
		String visitType = record.getValue("visit_type", String.class);
		Integer plotNo = record.getValue("plot_no", Integer.class);
		String plotSectionStr = record.getValue("plot_section", String.class);

		PlotSection plotSection = getPlotSection(clusterCode, plotNo, visitType, plotSectionStr);

		if ( plotSection == null ) {
			log.warn("Plot Section not found: " + (clusterCode + "_" + plotNo + "_" + plotSection + "_" + visitType));
			return null;
		}

		return plotSection;
	}
	
	@Override
	protected void onEnd() {
		List<PlotCategoricalValue> categoricalValues = new ArrayList<PlotCategoricalValue>();
		int cnt = 0;
		for ( PlotSection plot : values.keySet() ) {
			PlotSectionValue value = values.get(plot);
			Map<VariableMetadata, Category> categories = value.getCategories();
			
			for ( Category category : categories.values() ) {
				cnt += 1;
				
				PlotCategoricalValue pcv = new PlotCategoricalValue();
				pcv.setCategoryId(category.getCategoryId());
				pcv.setCurrent(true);
				pcv.setOriginal(false);
				pcv.setPlotSectionId(plot.getPlotSectionId());
				
				categoricalValues.add(pcv);
				
				if ( cnt % getInsertFrequency() == 0 ) {
					plotCategoricalValueDao.insert(categoricalValues);
					categoricalValues.clear();
				}
			}
			
		}
		if ( cnt % getInsertFrequency() != 0 ) {
			plotCategoricalValueDao.insert(categoricalValues);
			categoricalValues.clear();
		}
	
	}
	@Override
	protected void doInserts(List<PlotSection> obs, List<PlotNumericValue> numVals, List<PlotCategoricalValue> catVals) {
	}

	private PlotSection getPlotSection(String clusterCode, Integer plotNo, String visitType, String plotSection) {
		if ( clusterCode != null && visitType != null && plotNo != null && plotSection != null ) {
			// V.PLOT_OBS_UNIT_ID, V.CLUSTER_CODE, V.PLOT_NO, V.PLOT_SECTION, V.VISIT_TYPE)
			Integer id = plotSectionViewDao.getId( getObsUnitId(), clusterCode.trim(), plotNo, plotSection.trim(), visitType.trim() );
			if ( id != null ) {
				PlotSection ps = plotSectionDao.findById(id);
				return ps;
			}

		}

		return null;
	}

	@Override
	protected void onStart() {
		super.onStart();
		values = new HashMap<PlotSection, PlotSectionValue>();
		variables = new ArrayList<VariableMetadata>();
		
		ObservationUnitMetadata unit = getObservationUnitMetadata();
		Collection<VariableMetadata> vars = unit.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if( var.getVariableName().startsWith( "human_impact_" ) ) {
				variables.add(var);
			}			
		}
	}

	private int getObsUnitId() {
		ObservationUnitMetadata unitMetadata = getObservationUnitMetadata();
		return unitMetadata.getObsUnitId();
	}

	private VariableMetadata findVariableByCode(String code) {
		for ( VariableMetadata var : variables ) {
			String name = var.getVariableName();
			String varCode = name.substring( name.length() - 1);
			if( varCode.equals(code) ){
				return var;
			} 
		}
		return null;
	}
	
	static class PlotSectionValue {
		
		private Map<VariableMetadata, Category> categories;
		
		PlotSectionValue(PlotSection plotSection, List<VariableMetadata> vars ) {
			categories = new HashMap<VariableMetadata, Category>();
			
			for ( VariableMetadata var : vars ) {
				Category c = getFalseCategory(var);
				categories.put(var, c);
			}
		}
		
		public Map<VariableMetadata, Category> getCategories() {
			return categories;
		}
		
		void setValue( VariableMetadata var, String value ){
			if( StringUtils.isNotBlank(value) ){
				Category c = getTrueCategory(var);
				categories.put(var, c);
			}
		}
		
		private Category getTrueCategory(VariableMetadata var) {
			return getCategoryByValue(var, "T");
		}

		private Category getFalseCategory(VariableMetadata var) {
			return getCategoryByValue(var, "F");
		}
		
		private Category getCategoryByValue(VariableMetadata var, String value) {
			for ( Category c : var.getCategories() ) {
				if( c.getCategoryCode().equals(value) ){
					return c;
				}
			}
			return null;
		}
		
	}
	
}
