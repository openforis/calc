package org.openforis.calc.olap.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.openforis.calc.model.ObservationUnitMetadata;
import org.openforis.calc.model.SurveyMetadata;
import org.openforis.calc.model.VariableMetadata;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "annotations", "table", "view", "dimensionUsageOrDimension", "measure", "calculatedMember", "namedSet" })
public class Cube {

	@XmlElement(name = "Annotations")
	protected Annotations annotations;
	@XmlElement(name = "Table")
	protected Table table;
	@XmlElement(name = "View")
	protected View view;
	@XmlElements({ @XmlElement(name = "Dimension", type = PrivateDimension.class), @XmlElement(name = "DimensionUsage", type = DimensionUsage.class) })
	protected List<Object> dimensionUsageOrDimension;
	@XmlElement(name = "Measure", required = true)
	protected List<Measure> measure;
	@XmlElement(name = "CalculatedMember")
	protected List<CalculatedMember> calculatedMember;
	@XmlElement(name = "NamedSet")
	protected List<NamedSet> namedSet;
	@XmlAttribute(required = true)
	protected String name;
	@XmlAttribute
	protected String caption;
	@XmlAttribute
	protected String description;
	@XmlAttribute
	protected String defaultMeasure;
	@XmlAttribute
	protected Boolean cache;
	@XmlAttribute
	protected Boolean enabled;
	@XmlAttribute
	protected Boolean visible;

	@XmlTransient
	private ObservationUnitMetadata obsUnitMetadata;

	public Cube() {
	}

	public Cube(ObservationUnitMetadata obsUnitMetadata) {
		this.obsUnitMetadata = obsUnitMetadata;

		String name = obsUnitMetadata.isPlot() ? "Area" : "_" + Schema.getMdxName(obsUnitMetadata.getObsUnitName());
		setName(name);

		setCache(true);
		setEnabled(true);
		boolean visible = obsUnitMetadata.isPlot() ? true : false;
		setVisible(visible);

		initTable();
		initDimensions();
		initMeasures();
	}

	private void initMeasures() {
		if ( obsUnitMetadata.isPlot() ) {
			Measure areaMeasure = getAreaMeasure();
			getMeasures().add(areaMeasure);
		} else {
			Collection<VariableMetadata> vars = obsUnitMetadata.getVariableMetadata();
			for ( VariableMetadata var : vars ) {
				if ( var.isNumeric() && var.isForAnalysis() ) {
					Measure measure = new Measure(var);
					getMeasures().add(measure);
				}
			}
		}
	}

	private Measure getAreaMeasure() {
		Measure areaMeasure = new Measure();
		areaMeasure.setName("Area");
		areaMeasure.setColumn("est_area");
		areaMeasure.setFormatString(Measure.FORMAT_STRING_0_DECIMAL);
		areaMeasure.setAggregator(Measure.AGGREGATOR_SUM);
		areaMeasure.setCaption("Area");
		areaMeasure.setDescription("Estimated Area");
		areaMeasure.setVisible(true);
		return areaMeasure;
	}

	private void initDimensions() {
		DimensionUsage aoiDim = getAoiDimUsage();
		getDimensionUsageOrDimension().add(aoiDim);

		initDimensions(obsUnitMetadata);
		if ( obsUnitMetadata.isSpecimen() ) {
			ObservationUnitMetadata obsUnitParent = obsUnitMetadata.getObsUnitParent();
			initDimensions(obsUnitParent);

			DimensionUsage specimenDim = getSpecimenDimensionUsage();
			getDimensionUsageOrDimension().add(specimenDim);
		}
	}

	private DimensionUsage getSpecimenDimensionUsage() {
		DimensionUsage dim = new DimensionUsage();		
		// <DimensionUsage source="Species" name="Species" visible="true" foreignKey="specimen_taxon_id">
		// </DimensionUsage>
		dim.setSource("Species");
		dim.setName("Species");
		dim.setVisible(true);
		dim.setForeignKey("specimen_taxon_id");
		return dim;
	}

	private void initDimensions(ObservationUnitMetadata unitMetadata) {
		Collection<VariableMetadata> vars = unitMetadata.getVariableMetadata();
		for ( VariableMetadata var : vars ) {
			if ( var.isCategorical() && var.isForAnalysis() ) {
				DimensionUsage dim = new DimensionUsage(var);
				getDimensionUsageOrDimension().add(dim);
			}
		}
	}

	private DimensionUsage getAoiDimUsage() {
		DimensionUsage aoiDim = new DimensionUsage();
		aoiDim.setName("AOI");
		aoiDim.setSource("AOI");
		aoiDim.setVisible(true);
		aoiDim.setForeignKey("aoi_id");
		aoiDim.setHighCardinality(false);
		return aoiDim;
	}

	private void initTable() {
		Table table = new Table();
		String name = obsUnitMetadata.isPlot() ? "area_fact" : obsUnitMetadata.getObsUnitName() + "_fact";
		table.setName(name);

		SurveyMetadata surveyMetadata = obsUnitMetadata.getSurveyMetadata();
		table.setSchema(surveyMetadata.getSurveyName());

		setTable(table);
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
	 * Gets the value of the table property.
	 * 
	 * @return possible object is {@link Table }
	 * 
	 */
	public Table getTable() {
		return table;
	}

	/**
	 * Sets the value of the table property.
	 * 
	 * @param value
	 *            allowed object is {@link Table }
	 * 
	 */
	public void setTable(Table value) {
		this.table = value;
	}

	/**
	 * Gets the value of the view property.
	 * 
	 * @return possible object is {@link View }
	 * 
	 */
	public View getView() {
		return view;
	}

	/**
	 * Sets the value of the view property.
	 * 
	 * @param value
	 *            allowed object is {@link View }
	 * 
	 */
	public void setView(View value) {
		this.view = value;
	}

	/**
	 * Gets the value of the dimensionUsageOrDimension property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the dimensionUsageOrDimension property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getDimensionUsageOrDimension().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link PrivateDimension } {@link DimensionUsage }
	 * 
	 * 
	 */
	public List<Object> getDimensionUsageOrDimension() {
		if ( dimensionUsageOrDimension == null ) {
			dimensionUsageOrDimension = new ArrayList<Object>();
		}
		return this.dimensionUsageOrDimension;
	}

	/**
	 * Gets the value of the measure property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the returned list will be
	 * present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the measure property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getMeasure().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Measure }
	 * 
	 * 
	 */
	public List<Measure> getMeasures() {
		if ( measure == null ) {
			measure = new ArrayList<Measure>();
		}
		return this.measure;
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
	 * Gets the value of the cache property.
	 * 
	 * @return possible object is {@link Boolean }
	 * 
	 */
	public boolean isCache() {
		if ( cache == null ) {
			return true;
		} else {
			return cache;
		}
	}

	/**
	 * Sets the value of the cache property.
	 * 
	 * @param value
	 *            allowed object is {@link Boolean }
	 * 
	 */
	public void setCache(Boolean value) {
		this.cache = value;
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

	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}
}
