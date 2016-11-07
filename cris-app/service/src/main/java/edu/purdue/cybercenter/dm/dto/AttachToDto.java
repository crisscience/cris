//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2015.06.16 at 01:02:57 PM EDT
//


package edu.purdue.cybercenter.dm.dto;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="query" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="show-expression" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0}version-identifier"/>
 *       &lt;attGroup ref="{http://cyber.purdue.edu/cris/schemas/vocabulary/1.0.0}term-options"/>
 *       &lt;attribute name="id-field" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="name-field" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="use-alias" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "description",
    "query",
    "value",
    "showExpression",
    "grid"
})
@XmlRootElement(name = "attach-to")
public class AttachToDto {

    protected String description;
    protected String query;
    protected String value;
    @XmlElement(name = "show-expression")
    protected String showExpression;
    protected Boolean grid;
    @XmlAttribute(name = "id-field")
    protected String idField;
    @XmlAttribute(name = "name-field")
    protected String nameField;
    @XmlAttribute(name = "use-alias")
    protected String useAlias;
    @XmlAttribute(name = "versionName")
    protected String versionName;
    @XmlAttribute(name = "uuid", required = true)
    protected String uuid;
    @XmlAttribute(name = "version")
    protected String version;
    @XmlAttribute(name = "required")
    protected Boolean required;
    @XmlAttribute(name = "required-expression")
    protected String requiredExpression;
    @XmlAttribute(name = "read-only")
    protected Boolean readOnly;
    @XmlAttribute(name = "read-only-expression")
    protected String readOnlyExpression;
    @XmlAttribute(name = "list")
    protected Boolean list;
    @XmlAttribute(name = "ui-display-order")
    protected BigInteger uiDisplayOrder;

    protected Boolean isTermValid;
    protected Boolean isVersionValid;
    protected Boolean isLatest;
    protected String latestVersion;

    public Boolean getIsTermValid() {
        return isTermValid;
    }

    public void setIsTermValid(Boolean isTermValid) {
        this.isTermValid = isTermValid;
    }

    public Boolean getIsVersionValid() {
        return isVersionValid;
    }

    public void setIsVersionValid(Boolean isVersionValid) {
        this.isVersionValid = isVersionValid;
    }

    public Boolean getIsLatest() {
        return isLatest;
    }

    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
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
     * Gets the value of the query property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the value of the query property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setQuery(String value) {
        this.query = value;
    }

    /**
     * Gets the value of the value property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the showExpression property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getShowExpression() {
        return showExpression;
    }

    /**
     * Sets the value of the showExpression property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setShowExpression(String value) {
        this.showExpression = value;
    }

    /**
     * Gets the value of the grid property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isGrid() {
        return grid;
    }

    /**
     * Sets the value of the grid property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setGrid(Boolean value) {
        this.grid = value;
    }

    /**
     * Gets the value of the idField property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getIdField() {
        return idField;
    }

    /**
     * Sets the value of the idField property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setIdField(String value) {
        this.idField = value;
    }

    /**
     * Gets the value of the nameField property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getNameField() {
        return nameField;
    }

    /**
     * Sets the value of the nameField property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setNameField(String value) {
        this.nameField = value;
    }

    /**
     * Gets the value of the useAlias property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUseAlias() {
        return useAlias;
    }

    /**
     * Sets the value of the useAlias property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUseAlias(String value) {
        this.useAlias = value;
    }

    /**
     * Gets the value of the versionName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * Sets the value of the versionName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersionName(String value) {
        this.versionName = value;
    }

    /**
     * Gets the value of the uuid property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the value of the uuid property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setUuid(String value) {
        this.uuid = value;
    }

    /**
     * Gets the value of the version property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the required property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isRequired() {
        return required;
    }

    /**
     * Sets the value of the required property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setRequired(Boolean value) {
        this.required = value;
    }

    /**
     * Gets the value of the requiredExpression property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getRequiredExpression() {
        return requiredExpression;
    }

    /**
     * Sets the value of the requiredExpression property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setRequiredExpression(String value) {
        this.requiredExpression = value;
    }

    /**
     * Gets the value of the readOnly property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Sets the value of the readOnly property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setReadOnly(Boolean value) {
        this.readOnly = value;
    }

    /**
     * Gets the value of the readOnlyExpression property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getReadOnlyExpression() {
        return readOnlyExpression;
    }

    /**
     * Sets the value of the readOnlyExpression property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setReadOnlyExpression(String value) {
        this.readOnlyExpression = value;
    }

    /**
     * Gets the value of the list property.
     *
     * @return
     *     possible object is
     *     {@link Boolean }
     *
     */
    public Boolean isList() {
        return list;
    }

    /**
     * Sets the value of the list property.
     *
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *
     */
    public void setList(Boolean value) {
        this.list = value;
    }

    /**
     * Gets the value of the uiDisplayOrder property.
     *
     * @return
     *     possible object is
     *     {@link BigInteger }
     *
     */
    public BigInteger getUiDisplayOrder() {
        return uiDisplayOrder;
    }

    /**
     * Sets the value of the uiDisplayOrder property.
     *
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *
     */
    public void setUiDisplayOrder(BigInteger value) {
        this.uiDisplayOrder = value;
    }

}
