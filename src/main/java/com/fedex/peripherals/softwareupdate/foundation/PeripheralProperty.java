package com.fedex.peripherals.softwareupdate.foundation;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Model class for PeripheralProperty in peripheral server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = PeripheralSoftwareUpdateConstants.PERIPHERAL_PROPERTY)
public class PeripheralProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    String propname;
    @XmlAttribute
    String propvalue;

    public PeripheralProperty(String propname, String propvalue) {
        this.propname = propname;
        this.propvalue = propvalue;
    }

    public PeripheralProperty() {}

    public String getPropname() {
        return propname;
    }

    public String getPropvalue() {
        return propvalue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PeripheralProperty [propname=").append(propname).append(", propvalue=").append(propvalue)
                        .append("]");
        return builder.toString();
    }



}
