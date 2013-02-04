package org.openforis.calc.olap;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "dimensionGrant",
    "hierarchyGrant"
})
public class CubeGrant {

    @XmlElement(name = "DimensionGrant")
    protected List<DimensionGrant> dimensionGrant;
    @XmlElement(name = "HierarchyGrant")
    protected List<HierarchyGrant> hierarchyGrant;
    @XmlAttribute(required = true)
    protected String cube;
    @XmlAttribute(required = true)
    protected String access;

    /**
     * Gets the value of the dimensionGrant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dimensionGrant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDimensionGrant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DimensionGrant }
     * 
     * 
     */
    public List<DimensionGrant> getDimensionGrant() {
        if (dimensionGrant == null) {
            dimensionGrant = new ArrayList<DimensionGrant>();
        }
        return this.dimensionGrant;
    }

    /**
     * Gets the value of the hierarchyGrant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the hierarchyGrant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHierarchyGrant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HierarchyGrant }
     * 
     * 
     */
    public List<HierarchyGrant> getHierarchyGrant() {
        if (hierarchyGrant == null) {
            hierarchyGrant = new ArrayList<HierarchyGrant>();
        }
        return this.hierarchyGrant;
    }

    /**
     * Gets the value of the cube property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCube() {
        return cube;
    }

    /**
     * Sets the value of the cube property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCube(String value) {
        this.cube = value;
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