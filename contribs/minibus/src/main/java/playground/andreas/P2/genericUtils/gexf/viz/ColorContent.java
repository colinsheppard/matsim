//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-558 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.09.19 at 03:18:45 PM MESZ 
//


package playground.andreas.P2.genericUtils.gexf.viz;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import playground.andreas.P2.genericUtils.gexf.XMLSpellsContent;


/**
 * <p>Java class for color-content complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="color-content">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.gexf.net/1.2draft/viz}spells" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="r" use="required" type="{http://www.gexf.net/1.2draft/viz}color-channel" />
 *       &lt;attribute name="g" use="required" type="{http://www.gexf.net/1.2draft/viz}color-channel" />
 *       &lt;attribute name="b" use="required" type="{http://www.gexf.net/1.2draft/viz}color-channel" />
 *       &lt;attribute name="a" type="{http://www.gexf.net/1.2draft/viz}alpha-channel" />
 *       &lt;attribute name="start" type="{http://www.gexf.net/1.2draft}time-type" />
 *       &lt;attribute name="startopen" type="{http://www.gexf.net/1.2draft}time-type" />
 *       &lt;attribute name="end" type="{http://www.gexf.net/1.2draft}time-type" />
 *       &lt;attribute name="endopen" type="{http://www.gexf.net/1.2draft}time-type" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "color-content", propOrder = {
    "spells"
})
public class ColorContent {

    protected XMLSpellsContent spells;
    @XmlAttribute(required = true)
    protected int r;
    @XmlAttribute(required = true)
    protected int g;
    @XmlAttribute(required = true)
    protected int b;
    @XmlAttribute
    protected Float a;
    @XmlAttribute
    protected String start;
    @XmlAttribute
    protected String startopen;
    @XmlAttribute
    protected String end;
    @XmlAttribute
    protected String endopen;

    /**
     * Gets the value of the spells property.
     * 
     * @return
     *     possible object is
     *     {@link XMLSpellsContent }
     *     
     */
    public XMLSpellsContent getSpells() {
        return spells;
    }

    /**
     * Sets the value of the spells property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLSpellsContent }
     *     
     */
    public void setSpells(XMLSpellsContent value) {
        this.spells = value;
    }

    /**
     * Gets the value of the r property.
     * 
     */
    public int getR() {
        return r;
    }

    /**
     * Sets the value of the r property.
     * 
     */
    public void setR(int value) {
        this.r = value;
    }

    /**
     * Gets the value of the g property.
     * 
     */
    public int getG() {
        return g;
    }

    /**
     * Sets the value of the g property.
     * 
     */
    public void setG(int value) {
        this.g = value;
    }

    /**
     * Gets the value of the b property.
     * 
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the value of the b property.
     * 
     */
    public void setB(int value) {
        this.b = value;
    }

    /**
     * Gets the value of the a property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getA() {
        return a;
    }

    /**
     * Sets the value of the a property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setA(Float value) {
        this.a = value;
    }

    /**
     * Gets the value of the start property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Gets the value of the startopen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartopen() {
        return startopen;
    }

    /**
     * Sets the value of the startopen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartopen(String value) {
        this.startopen = value;
    }

    /**
     * Gets the value of the end property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnd(String value) {
        this.end = value;
    }

    /**
     * Gets the value of the endopen property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEndopen() {
        return endopen;
    }

    /**
     * Sets the value of the endopen property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEndopen(String value) {
        this.endopen = value;
    }

}
