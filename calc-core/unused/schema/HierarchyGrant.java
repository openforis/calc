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
    "memberGrant"
})
public class HierarchyGrant {

    @XmlElement(name = "MemberGrant")
    protected List<HierarchyGrant.MemberGrant> memberGrant;
    @XmlAttribute(required = true)
    protected String hierarchy;
    @XmlAttribute(required = true)
    protected String access;
    @XmlAttribute
    protected String topLevel;
    @XmlAttribute
    protected String bottomLevel;
    @XmlAttribute
    protected String rollupPolicy;

    /**
     * Gets the value of the memberGrant property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the memberGrant property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMemberGrant().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link HierarchyGrant.MemberGrant }
     * 
     * 
     */
    public List<HierarchyGrant.MemberGrant> getMemberGrant() {
        if (memberGrant == null) {
            memberGrant = new ArrayList<HierarchyGrant.MemberGrant>();
        }
        return this.memberGrant;
    }

    /**
     * Gets the value of the hierarchy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHierarchy() {
        return hierarchy;
    }

    /**
     * Sets the value of the hierarchy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHierarchy(String value) {
        this.hierarchy = value;
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

    /**
     * Gets the value of the topLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTopLevel() {
        return topLevel;
    }

    /**
     * Sets the value of the topLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTopLevel(String value) {
        this.topLevel = value;
    }

    /**
     * Gets the value of the bottomLevel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBottomLevel() {
        return bottomLevel;
    }

    /**
     * Sets the value of the bottomLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBottomLevel(String value) {
        this.bottomLevel = value;
    }

    /**
     * Gets the value of the rollupPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRollupPolicy() {
        if (rollupPolicy == null) {
            return "full";
        } else {
            return rollupPolicy;
        }
    }

    /**
     * Sets the value of the rollupPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRollupPolicy(String value) {
        this.rollupPolicy = value;
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
     *       &lt;attribute name="member" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="access" use="required">
     *         &lt;simpleType>
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *             &lt;enumeration value="all"/>
     *             &lt;enumeration value="none"/>
     *           &lt;/restriction>
     *         &lt;/simpleType>
     *       &lt;/attribute>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class MemberGrant {

        @XmlAttribute
        protected String member;
        @XmlAttribute(required = true)
        protected String access;

        /**
         * Gets the value of the member property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getMember() {
            return member;
        }

        /**
         * Sets the value of the member property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setMember(String value) {
            this.member = value;
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

}