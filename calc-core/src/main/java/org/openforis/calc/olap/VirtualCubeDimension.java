package org.openforis.calc.olap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class VirtualCubeDimension {

	@XmlAttribute
	protected String cubeName;
	@XmlAttribute
	protected String name;
	@XmlAttribute
	protected Boolean highCardinality;
	@XmlAttribute
	protected Boolean visible;

	public VirtualCubeDimension() {
	}

	public VirtualCubeDimension(VariableMetadata var) {
		ObservationUnitMetadata obsUnit = var.getObservationUnitMetadata();

		String cube = obsUnit.isTypePlot() ? "Area" : "_" + Schema.getMdxName(obsUnit.getObsUnitName());
		setCubeName(cube);
		
		String name = Schema.getMdxName(var.getVariableName());
		setName(name);
		
		setHighCardinality(false);
		setVisible(true);
	}

	/**
	 * Gets the value of the cubeName property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCubeName() {
		return cubeName;
	}

	/**
	 * Sets the value of the cubeName property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCubeName(String value) {
		this.cubeName = value;
	}

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value) {
		this.name = value;
	}

	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public Boolean isHighCardinality() {
		return highCardinality;
	}

	public void setHighCardinality(Boolean highCardinality) {
		this.highCardinality = highCardinality;
	}

}
