//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.20 at 07:21:37 PM MESZ 
//


package org.matsim.jaxb.signalsystems11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for signalSystemDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="signalSystemDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.matsim.org/files/dtd}matsimObjectType">
 *       &lt;sequence>
 *         &lt;element name="defaultCycleTime" type="{http://www.matsim.org/files/dtd}matsimTimeAttributeType" minOccurs="0"/>
 *         &lt;element name="defaultSynchronizationOffset" type="{http://www.matsim.org/files/dtd}matsimTimeAttributeType" minOccurs="0"/>
 *         &lt;element name="defaultInterGreenTime" type="{http://www.matsim.org/files/dtd}matsimTimeAttributeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "signalSystemDefinitionType", propOrder = {
    "defaultCycleTime",
    "defaultSynchronizationOffset",
    "defaultInterGreenTime"
})
public class XMLSignalSystemDefinitionType
    extends XMLMatsimObjectType
{

    protected XMLMatsimTimeAttributeType defaultCycleTime;
    protected XMLMatsimTimeAttributeType defaultSynchronizationOffset;
    protected XMLMatsimTimeAttributeType defaultInterGreenTime;

    /**
     * Gets the value of the defaultCycleTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLMatsimTimeAttributeType }
     *     
     */
    public XMLMatsimTimeAttributeType getDefaultCycleTime() {
        return defaultCycleTime;
    }

    /**
     * Sets the value of the defaultCycleTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLMatsimTimeAttributeType }
     *     
     */
    public void setDefaultCycleTime(XMLMatsimTimeAttributeType value) {
        this.defaultCycleTime = value;
    }

    /**
     * Gets the value of the defaultSynchronizationOffset property.
     * 
     * @return
     *     possible object is
     *     {@link XMLMatsimTimeAttributeType }
     *     
     */
    public XMLMatsimTimeAttributeType getDefaultSynchronizationOffset() {
        return defaultSynchronizationOffset;
    }

    /**
     * Sets the value of the defaultSynchronizationOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLMatsimTimeAttributeType }
     *     
     */
    public void setDefaultSynchronizationOffset(XMLMatsimTimeAttributeType value) {
        this.defaultSynchronizationOffset = value;
    }

    /**
     * Gets the value of the defaultInterGreenTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLMatsimTimeAttributeType }
     *     
     */
    public XMLMatsimTimeAttributeType getDefaultInterGreenTime() {
        return defaultInterGreenTime;
    }

    /**
     * Sets the value of the defaultInterGreenTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLMatsimTimeAttributeType }
     *     
     */
    public void setDefaultInterGreenTime(XMLMatsimTimeAttributeType value) {
        this.defaultInterGreenTime = value;
    }

}
