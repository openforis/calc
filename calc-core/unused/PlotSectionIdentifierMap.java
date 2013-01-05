package org.openforis.calc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author G. Miceli
 */
public class PlotSectionIdentifierMap {
	private Map<String, Integer> map;
	
	public PlotSectionIdentifierMap() {
		map = new HashMap<String, Integer>();
	}

	public void put(String clusterCode, int plotNo, String surveyType, int sectionId) {
		String key = getKey(clusterCode, plotNo, surveyType);
		map.put(key, sectionId);
	}
	
	private String getKey(String clusterCode, int plotNo, String surveyType) {
		return clusterCode+"/"+plotNo+"/"+surveyType;
	}

	public Integer getId(String clusterCode, int plotNo, String surveyType, int sectionId) {
		String key = getKey(clusterCode, plotNo, surveyType);
		return map.get(key);
	}
}
