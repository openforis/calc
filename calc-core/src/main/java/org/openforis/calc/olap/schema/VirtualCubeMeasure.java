package org.openforis.calc.olap.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openforis.calc.model.VariableMetadata;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "annotations" })
public class VirtualCubeMeasure {

	@XmlElement(name = "Annotations")
	protected Annotations annotations;
	@XmlAttribute(required = true)
	protected String cubeName;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	protected Boolean visible;
	
	public VirtualCubeMeasure() {
	}

	public VirtualCubeMeasure(VariableMetadata var) {
		if( ! var.isNumeric() && ! var.isForAnalysis()){
			throw new IllegalArgumentException("Invalid variable metadata. Only numeric and for analysis is allowed");
		}
		
		setVisible(true);
		
		String cubeName = "_" + Schema.getMdxName( var.getObservationUnitMetadata().getObsUnitName() );
		setCubeName(cubeName);
		
		String measureName = Measure.getMeasureMdxName(var.getVariableName());
		setName(measureName);
	}
	
	/**
	 * Gets the value of the annotations property.
	 * 
	 * @return possible object is {@link Annotations }
	 * 
	 */
	public Annotations getAnnotations() {
		return annotations;
	}

	/**
	 * Sets the value of the annotations property.
	 * 
	 * @param value
	 *            allowed object is {@link Annotations }
	 * 
	 */
	public void setAnnotations(Annotations value) {
		this.annotations = value;
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

	/**
	 * Gets the value of the visible property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isVisible() {
		if ( visible == null ) {
			return true;
		} else {
			return visible;
		}
	}

	/**
	 * Sets the value of the visible property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setVisible(Boolean value) {
		this.visible = value;
	}

}