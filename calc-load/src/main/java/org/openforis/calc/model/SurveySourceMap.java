package org.openforis.calc.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author G. Miceli
 *
 */
public class SurveySourceMap {
	private Map<Integer, ModelObject> map;
	
	public SurveySourceMap() {
		map = new HashMap<Integer, ModelObject>();
	}
	
	public ModelObject getModelObject(int sourceId) {
		return map.get(sourceId);
	}
	
	public void setModelObject(int sourceId, ModelObject obj) {
		map.put(sourceId, obj);
	}
}
