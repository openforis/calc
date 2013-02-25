package org.openforis.calc.model;

/**
 * @author G. Miceli
 */
public interface Value extends Identifiable {
	Integer getObservationId();
	void setObservationId(Integer obsId);
	Boolean getOriginal();
	void setOriginal(Boolean original);
	Boolean getCurrent();
	void setCurrent(Boolean current);
}
