package org.openforis.calc.olap.schema;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class DimensionGrant {

    @XmlAttribute(required = true)
    protected String dimension;
    @XmlAttribute(required = true)
    protected String access;

    /**
     * Gets the value of the dimension property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDimension() {
        return dimension;
    }

    /**
     * Sets the value of the dimension property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDimension(String value) {
        this.dimension = value;
    }

    /**
     * Gets the value of the access property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAccess() {
        return access;
    }

    /**
     * Sets the value of the access property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccess(String value) {
        this.access = value;
    }

}