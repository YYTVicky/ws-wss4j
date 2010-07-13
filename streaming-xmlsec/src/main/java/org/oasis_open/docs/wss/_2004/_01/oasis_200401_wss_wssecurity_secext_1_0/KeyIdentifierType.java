//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.05.13 at 04:42:25 PM CEST 
//


package org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0;

import ch.gigerstyle.xmlsec.ext.Constants;
import ch.gigerstyle.xmlsec.ext.ParseException;
import ch.gigerstyle.xmlsec.ext.Parseable;
import ch.gigerstyle.xmlsec.ext.Utils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;


/**
 * A security token key identifier
 * 
 * <p>Java class for KeyIdentifierType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KeyIdentifierType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd>EncodedString">
 *       &lt;attribute name="ValueType" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeyIdentifierType")
public class KeyIdentifierType
    extends EncodedString implements Parseable
{

    @XmlAttribute(name = "ValueType")
    @XmlSchemaType(name = "anyURI")
    protected String valueType;

    public KeyIdentifierType(StartElement startElement) {
        super(startElement);
        Iterator<Attribute> attributeIterator = startElement.getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (attribute.getName().equals(Constants.ATT_NULL_ValueType)) {
                 this.valueType = attribute.getValue();
             }
        }
    }

    public boolean parseXMLEvent(XMLEvent xmlEvent) throws ParseException {

        super.parseXMLEvent(xmlEvent);

        switch (xmlEvent.getEventType()) {
             case XMLStreamConstants.START_ELEMENT:
                 break;
             case XMLStreamConstants.END_ELEMENT:
                 EndElement endElement = xmlEvent.asEndElement();
                 if (endElement.getName().equals(Constants.TAG_wsse_KeyIdentifier)) {
                     return true;
                 }
                 break;
             case XMLStreamConstants.CHARACTERS:
                 break;
             default:
                 throw new ParseException("Unexpected event received " + Utils.getXMLEventAsString(xmlEvent));
        }
        return false;
    }

    public void validate() throws ParseException {
        super.validate();
    }

    /**
     * Gets the value of the valueType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueType() {
        return valueType;
    }

    /**
     * Sets the value of the valueType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueType(String value) {
        this.valueType = value;
    }

}
