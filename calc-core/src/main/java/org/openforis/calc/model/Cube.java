package org.openforis.calc.model;

import java.util.List;

/**
 * 
 * @author G. Miceli
 * @author M. Togna
 *
 */
public class Cube extends org.openforis.calc.persistence.jooq.tables.pojos.Cube {

	private static final long serialVersionUID = 1L;

	private org.openforis.calc.persistence.jooq.tables.pojos.Cube cube;
	private List<Dimension> dimensions;
	private List<Measure> measures;
		
	public boolean equals(Object obj) {
		return cube.equals(obj);
	}

	public Integer getCubeId() {
		return cube.getCubeId();
	}

	public String getCubeName() {
		return cube.getCubeName();
	}

	public String getCubeCaption() {
		return cube.getCubeCaption();
	}

	public Boolean getVisible() {
		return cube.getVisible();
	}

	public Integer getSurveyId() {
		return cube.getSurveyId();
	}

	public int hashCode() {
		return cube.hashCode();
	}

	public void setCubeId(Integer cubeId) {
		cube.setCubeId(cubeId);
	}

	public void setCubeName(String cubeName) {
		cube.setCubeName(cubeName);
	}

	public void setCubeCaption(String cubeCaption) {
		cube.setCubeCaption(cubeCaption);
	}

	public void setVisible(Boolean visible) {
		cube.setVisible(visible);
	}

	public void setSurveyId(Integer surveyId) {
		cube.setSurveyId(surveyId);
	}

	public String toString() {
		return cube.toString();
	}
}
