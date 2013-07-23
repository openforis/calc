package org.openforis.calc.olap.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.VariableMetadata;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "annotations", "cubeUsages", "virtualCubeDimension", "virtualCubeMeasure", "calculatedMember", "namedSet" })
public class VirtualCube {

	@XmlElement(name = "Annotations")
	protected Annotations annotations;
	@XmlElement(name = "CubeUsages", required = true)
	protected VirtualCube.CubeUsages cubeUsages;
	@XmlElement(name = "VirtualCubeDimension", required = true)
	protected List<VirtualCubeDimension> virtualCubeDimension;
	@XmlElement(name = "VirtualCubeMeasure", required = true)
	protected List<VirtualCubeMeasure> virtualCubeMeasure;
	@XmlElement(name = "CalculatedMember")
	protected List<CalculatedMember> calculatedMember;
	@XmlElement(name = "NamedSet")
	protected List<NamedSet> namedSet;
	@XmlAttribute
	protected Boolean enabled;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	protected String defaultMeasure;
	@XmlAttribute
	protected String caption;
	@XmlAttribute
	protected String description;
	@XmlAttribute
	protected Boolean visible;

	@XmlTransient
	private ObservationUnitMetadata obsUnitMetadata;

	public VirtualCube() {
	}

	public VirtualCube(ObservationUnitMetadata obsUnitMetadata) {
		if ( !obsUnitMetadata.isSpecimen() ) {
			throw new IllegalArgumentException("Virtual cube can be created only from specimen observation unit");
		}
		this.obsUnitMetadata = obsUnitMetadata;
		String name = Schema.getMdxName(obsUnitMetadata.getObsUnitName());
		setEnabled(true);
		setVisible(true);
		setName(name);
		initMembers();
	}

	private void initMembers() {
		VirtualCubeDimension aoiDim = getDimension("AOI");		
		VirtualCubeDimension speciesDim = getDimension("Species");
		getVirtualCubeDimensions().add(aoiDim);
		getVirtualCubeDimensions().add(speciesDim);
		
		VirtualCubeMeasure areaMeasure = getAreaMeasure();
		getVirtualCubeMeasures().add(areaMeasure);
		
		Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if ( var.isForAnalysis() ) {
				if ( var.isCategorical() ) {
					VirtualCubeDimension dim = new VirtualCubeDimension( var );
					getVirtualCubeDimensions().add(dim);
				} else if ( var.isNumeric() ) {
					VirtualCubeMeasure measure = new VirtualCubeMeasure( var );
					getVirtualCubeMeasures().add(measure);
					CalculatedMember calcMember = new CalculatedMember( var );
					getCalculatedMember().add(calcMember );
				}
			}
		}

		
		// add dimensions from parent observation unit metadata
		ObservationUnitMetadata unitParent = obsUnitMetadata.getObsUnitParent();
		Collection<VariableMetadata> parentVars = unitParent.getVariableMetadata();
		for ( VariableMetadata var : parentVars ) {
			if( var.isCategorical() && var.isForAnalysis() ){
				VirtualCubeDimension dim = new VirtualCubeDimension( var );
				getVirtualCubeDimensions().add(dim);
			}
		}
		
	}

	/**
	 * 	<VirtualCubeMeasure cubeName="Area" name="[Measures].[Area]" visible="false">
    	</VirtualCubeMeasure>
	 * @return
	 */
	private VirtualCubeMeasure getAreaMeasure() {
		VirtualCubeMeasure area = new VirtualCubeMeasure();
		area.setCubeName("Area");
		area.setName(Measure.getMeasureMdxName("Area"));
		area.setVisible(false);
		return area;
	}

	private VirtualCubeDimension getDimension(String name) {
		VirtualCubeDimension dimension = new VirtualCubeDimension();
		dimension.setName(name);
		String cubeName = "_" + Schema.getMdxName(obsUnitMetadata.getObsUnitName());
		dimension.setCubeName(cubeName);
		dimension.setVisible(true);
		dimension.setHighCardinality(false);
		return dimension;
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
	 * Gets the value of the cubeUsages property.
	 * 
	 * @return possible object is {@link VirtualCube.CubeUsages }
	 * 
	 */
	public VirtualCube.CubeUsages getCubeUsages() {
		return cubeUsages;
	}

	/**
	 * Sets the value of the cubeUsages property.
	 * 
	 * @param value
	 *            allowed object is {@link VirtualCube.CubeUsages }
	 * 
	 */
	public void setCubeUsages(VirtualCube.CubeUsages value) {
		this.cubeUsages = value;
	}

	/**
	 * Gets the value of the virtualCubeDimension property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the virtualCubeDimension property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getVirtualCubeDimension().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link VirtualCubeDimension }
	 * 
	 * 
	 */
	public List<VirtualCubeDimension> getVirtualCubeDimensions() {
		if ( virtualCubeDimension == null ) {
			virtualCubeDimension = new ArrayList<VirtualCubeDimension>();
		}
		return this.virtualCubeDimension;
	}

	/**
	 * Gets the value of the virtualCubeMeasure property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the virtualCubeMeasure property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getVirtualCubeMeasure().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link VirtualCubeMeasure }
	 * 
	 * 
	 */
	public List<VirtualCubeMeasure> getVirtualCubeMeasures() {
		if ( virtualCubeMeasure == null ) {
			virtualCubeMeasure = new ArrayList<VirtualCubeMeasure>();
		}
		return this.virtualCubeMeasure;
	}

	/**
	 * Gets the value of the calculatedMember property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the calculatedMember property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getCalculatedMember().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link CalculatedMember }
	 * 
	 * 
	 */
	public List<CalculatedMember> getCalculatedMember() {
		if ( calculatedMember == null ) {
			calculatedMember = new ArrayList<CalculatedMember>();
		}
		return this.calculatedMember;
	}

	/**
	 * Gets the value of the namedSet property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the namedSet property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getNamedSet().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link NamedSet }
	 * 
	 * 
	 */
	public List<NamedSet> getNamedSet() {
		if ( namedSet == null ) {
			namedSet = new ArrayList<NamedSet>();
		}
		return this.namedSet;
	}

	/**
	 * Gets the value of the enabled property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isEnabled() {
		if ( enabled == null ) {
			return true;
		} else {
			return enabled;
		}
	}

	/**
	 * Sets the value of the enabled property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setEnabled(Boolean value) {
		this.enabled = value;
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
	 * Gets the value of the defaultMeasure property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDefaultMeasure() {
		return defaultMeasure;
	}

	/**
	 * Sets the value of the defaultMeasure property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDefaultMeasure(String value) {
		this.defaultMeasure = value;
	}

	/**
	 * Gets the value of the caption property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * Sets the value of the caption property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setCaption(String value) {
		this.caption = value;
	}

	/**
	 * Gets the value of the description property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the value of the description property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setDescription(String value) {
		this.description = value;
	}

	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "cubeUsage" })
	public static class CubeUsages {

		@XmlElement(name = "CubeUsage", required = true)
		protected List<VirtualCube.CubeUsages.CubeUsage> cubeUsage;

		/**
		 * Gets the value of the cubeUsage property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
		 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the cubeUsage property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getCubeUsage().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link VirtualCube.CubeUsages.CubeUsage }
		 * 
		 * 
		 */
		public List<VirtualCube.CubeUsages.CubeUsage> getCubeUsage() {
			if ( cubeUsage == null ) {
				cubeUsage = new ArrayList<VirtualCube.CubeUsages.CubeUsage>();
			}
			return this.cubeUsage;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(name = "")
		public static class CubeUsage {

			@XmlAttribute(required = true)
			protected String cubeName;
			@XmlAttribute
			protected Boolean ignoreUnrelatedDimensions;

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
			 * Gets the value of the ignoreUnrelatedDimensions property.
			 * 
			 * @return possible object is {@link Boolean }
			 * 
			 */
			public boolean isIgnoreUnrelatedDimensions() {
				if ( ignoreUnrelatedDimensions == null ) {
					return false;
				} else {
					return ignoreUnrelatedDimensions;
				}
			}

			/**
			 * Sets the value of the ignoreUnrelatedDimensions property.
			 * 
			 * @param value
			 *            allowed object is {@link Boolean }
			 * 
			 */
			public void setIgnoreUnrelatedDimensions(Boolean value) {
				this.ignoreUnrelatedDimensions = value;
			}

		}

	}

}
