package org.openforis.calc.olap.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.openforis.calc.model.VariableMetadata;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "annotations",
    "measureExpression",
    "calculatedMemberProperty"
})
public class Measure {

	static final String AGGREGATOR_SUM = "sum";
	static final String FORMAT_STRING_0_DECIMAL = "#,###";
	static final String FORMAT_STRING_5_DECIMAL = "#,###.#####";

	static final String MEASURES_SEPARATOR = ".";
	static final String MEASURE_PREFIX = "[Measures]";
	
    @XmlElement(name = "Annotations")
    protected Annotations annotations;
    @XmlElement(name = "MeasureExpression")
    protected ExpressionView measureExpression;
    @XmlElement(name = "CalculatedMemberProperty")
    protected List<CalculatedMemberProperty> calculatedMemberProperty;
    @XmlAttribute(required = true)
    protected String name;
    @XmlAttribute
    protected String column;
    @XmlAttribute
    protected String datatype;
    @XmlAttribute
    protected String formatString;
    @XmlAttribute(required = true)
    protected String aggregator;
    @XmlAttribute
    protected String formatter;
    @XmlAttribute
    protected String caption;
    @XmlAttribute
    protected String description;
    @XmlAttribute
    protected Boolean visible;
    

    public Measure(VariableMetadata variableMetadata) {
    	String varName = variableMetadata.getVariableName();
    	String mdxName = Schema.getMdxName(varName);
    	
    	setName(mdxName);
    	setColumn( varName );
    	setDatatype("Numeric");
    	setAggregator(AGGREGATOR_SUM);
    	setFormatString(FORMAT_STRING_5_DECIMAL);
    	setVisible(true);
    	//TODO add caption with unit
    	setCaption(mdxName);
	} 
    	
	public Measure() {
	}

	/**
     * Gets the value of the annotations property.
     * 
     * @return
     *     possible object is
     *     {@link Annotations }
     *     
     */
    public Annotations getAnnotations() {
        return annotations;
    }

    /**
     * Sets the value of the annotations property.
     * 
     * @param value
     *     allowed object is
     *     {@link Annotations }
     *     
     */
    public void setAnnotations(Annotations value) {
        this.annotations = value;
    }

    /**
     * Gets the value of the measureExpression property.
     * 
     * @return
     *     possible object is
     *     {@link ExpressionView }
     *     
     */
    public ExpressionView getMeasureExpression() {
        return measureExpression;
    }

    /**
     * Sets the value of the measureExpression property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpressionView }
     *     
     */
    public void setMeasureExpression(ExpressionView value) {
        this.measureExpression = value;
    }

    /**
     * Gets the value of the calculatedMemberProperty property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the calculatedMemberProperty property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCalculatedMemberProperty().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CalculatedMemberProperty }
     * 
     * 
     */
    public List<CalculatedMemberProperty> getCalculatedMemberProperty() {
        if (calculatedMemberProperty == null) {
            calculatedMemberProperty = new ArrayList<CalculatedMemberProperty>();
        }
        return this.calculatedMemberProperty;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the column property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColumn() {
        return column;
    }

    /**
     * Sets the value of the column property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColumn(String value) {
        this.column = value;
    }

    /**
     * Gets the value of the datatype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * Sets the value of the datatype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDatatype(String value) {
        this.datatype = value;
    }

    /**
     * Gets the value of the formatString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatString() {
        return formatString;
    }

    /**
     * Sets the value of the formatString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatString(String value) {
        this.formatString = value;
    }

    /**
     * Gets the value of the aggregator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAggregator() {
        return aggregator;
    }

    /**
     * Sets the value of the aggregator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAggregator(String value) {
        this.aggregator = value;
    }

    /**
     * Gets the value of the formatter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatter() {
        return formatter;
    }

    /**
     * Sets the value of the formatter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatter(String value) {
        this.formatter = value;
    }

    /**
     * Gets the value of the caption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Sets the value of the caption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCaption(String value) {
        this.caption = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the visible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVisible() {
        if (visible == null) {
            return true;
        } else {
            return visible;
        }
    }

    /**
     * Sets the value of the visible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVisible(Boolean value) {
        this.visible = value;
    }

	static String getMeasureMdxName(String name){
		String mdxName = Schema.getMdxName(name);
		
		StringBuilder sb = new StringBuilder();
		sb.append(MEASURE_PREFIX);
		sb.append(MEASURES_SEPARATOR);
		sb.append("[");
		sb.append(mdxName);
		sb.append("]");
		return sb.toString();
	}

}