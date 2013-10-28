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
    "annotations",
    "schemaGrants",
    "union"
})
public class Role {

    @XmlElement(name = "Annotations")
    protected Annotations annotations;
    @XmlElement(name = "SchemaGrant")
    protected List<SchemaGrant> schemaGrants;
    @XmlElement(name = "Union", required = true)
    protected Role.Union union;
    @XmlAttribute(required = true)
    protected String name;

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
     * Gets the value of the schemaGrant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the schemaGrant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSchemaGrant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SchemaGrant }
     * 
     * 
     */
    public List<SchemaGrant> getSchemaGrants() {
        if (schemaGrants == null) {
            schemaGrants = new ArrayList<SchemaGrant>();
        }
        return this.schemaGrants;
    }

    
    public void addSchemaGrant(SchemaGrant schemaGrant){
    	getSchemaGrants().add(schemaGrant);
    }
    /**
     * Gets the value of the union property.
     * 
     * @return
     *     possible object is
     *     {@link Role.Union }
     *     
     */
    public Role.Union getUnion() {
        return union;
    }

    /**
     * Sets the value of the union property.
     * 
     * @param value
     *     allowed object is
     *     {@link Role.Union }
     *     
     */
    public void setUnion(Role.Union value) {
        this.union = value;
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
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="RoleUsage" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="roleName" type="{http://www.w3.org/2001/XMLSchema}string" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "roleUsage"
    })
    public static class Union {

        @XmlElement(name = "RoleUsage", required = true)
        protected List<Role.Union.RoleUsage> roleUsage;

        /**
         * Gets the value of the roleUsage property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the roleUsage property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getRoleUsage().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Role.Union.RoleUsage }
         * 
         * 
         */
        public List<Role.Union.RoleUsage> getRoleUsage() {
            if (roleUsage == null) {
                roleUsage = new ArrayList<Role.Union.RoleUsage>();
            }
            return this.roleUsage;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="roleName" type="{http://www.w3.org/2001/XMLSchema}string" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class RoleUsage {

            @XmlAttribute
            protected String roleName;

            /**
             * Gets the value of the roleName property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getRoleName() {
                return roleName;
            }

            /**
             * Sets the value of the roleName property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setRoleName(String value) {
                this.roleName = value;
            }

        }

    }

}