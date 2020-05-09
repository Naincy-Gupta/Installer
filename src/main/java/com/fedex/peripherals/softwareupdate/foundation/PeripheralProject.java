package com.fedex.peripherals.softwareupdate.foundation;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Model class for PeripheralProject in peripheral server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = PeripheralSoftwareUpdateConstants.PERIPHERAL_PROJECT)
public class PeripheralProject {

    @XmlElement(name = PeripheralSoftwareUpdateConstants.PERIPHERAL_ACTION)
    List<PeripheralAction> actions;
    @XmlAttribute
    String name;
    @XmlAttribute
    String description;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<PeripheralAction> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PeripheralProject [actions=").append(actions).append(", name=").append(name)
                        .append(", description=").append(description).append("]");
        return builder.toString();
    }

   
}
