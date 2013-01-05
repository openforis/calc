package org.openforis.calc.model;

/**
 * 
 * @author G. Miceli
 *
 */
public interface ImportableModelObject extends ModelObject {
	Integer getSourceId();
	void setSourceId(Integer sourceId);
}
