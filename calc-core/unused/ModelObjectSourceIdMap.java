package org.openforis.calc.dataimport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.calc.model.ImportableModelObject;

/**
 * 
 * @author G. Miceli
 *
 */
public class ModelObjectSourceIdMap {
	private Map<Integer, ImportableModelObject> modelObjects;
	
	public ModelObjectSourceIdMap() {
		modelObjects = new HashMap<Integer, ImportableModelObject>();
	}
	
	public ImportableModelObject getModelObject(int sourceId) {
		return (ImportableModelObject) modelObjects.get(sourceId);
	}
	
	public void putModelObject(ImportableModelObject obj) {
		modelObjects.put(obj.getSourceId(), obj);
	}
	
	public void putModelObjects(List<? extends ImportableModelObject> objs) {
		for (ImportableModelObject obj : objs) {
			putModelObject(obj);
		}
	}
}
