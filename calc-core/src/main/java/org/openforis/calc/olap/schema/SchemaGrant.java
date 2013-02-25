package org.openforis.calc.olap.schema;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "cubeGrant"
})
public class SchemaGrant {

	@XmlElement(name = "CubeGrant", required = true)
    protected List<CubeGrant> cubeGrant;
    @XmlAttribute(required = true)
    protected String access;

    /**
     * Gets the value of the cubeGrant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cubeGrant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCubeGrant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CubeGrant }
     * 
     * 
     */
    public List<CubeGrant> getCubeGrants() {
        if (cubeGrant == null) {
            cubeGrant = new ArrayList<CubeGrant>();
        }
        return this.cubeGrant;
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

	void addCubeGrant(CubeGrant cubeGrant) {
		getCubeGrants().add(cubeGrant);
		
	}

}