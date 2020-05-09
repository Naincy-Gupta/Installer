package com.fedex.peripherals.softwareupdate.foundation;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Model class for PeripheralAction in peripheral server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = PeripheralSoftwareUpdateConstants.PERIPHERAL_ACTION)
public class PeripheralAction implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlElement(name = PeripheralSoftwareUpdateConstants.PERIPHERAL_PROPERTY)
    List<PeripheralProperty> property;
    @XmlAttribute(name = PeripheralSoftwareUpdateConstants.NAME, required = true)
    String name;
    @XmlAttribute
    String description;
    @XmlAttribute(name = PeripheralSoftwareUpdateConstants.CLASS, required = true)
    String className;
    @XmlAttribute
    String rollback;
    @XmlAttribute(name = PeripheralSoftwareUpdateConstants.ACTIVE, required = true)
    String active;
    @XmlAttribute(name = PeripheralSoftwareUpdateConstants.EXIT_ON_FAIL)
    String exitOnFail;
    @XmlAttribute(name = PeripheralSoftwareUpdateConstants.PHASE)
    PeripheralSoftwarePhasesErrorCode peripheralSoftwareUpdatePhase;
    @XmlAttribute(name = PeripheralSoftwareUpdateConstants.RETRY_COUNT)
    int retryCount;

    public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public boolean isExitOfFail() {
        return Boolean.valueOf(exitOnFail);
    }

    public boolean isActive() {
        return Boolean.valueOf(this.active);
    }

    public String getName() {
        return name;
    }

    public List<PeripheralProperty> getProperty() {
        return property;
    }


    public String getDescription() {
        return description;
    }


    public String getClassName() {
        return className;
    }

    public boolean isRollback() {
        return Boolean.valueOf(this.rollback);
    }
    
    public PeripheralSoftwarePhasesErrorCode getPeripheralSoftwareUpdatePhase() {
        return peripheralSoftwareUpdatePhase;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PeripheralAction [property=").append(property).append(", name=").append(name)
                        .append(", description=").append(description).append(", className=").append(className)
                        .append(", rollback=").append(rollback).append(", active=").append(active)
                        .append(", exitOnFail=").append(exitOnFail).append(", peripheralSoftwareUpdatePhase=")
                        .append(peripheralSoftwareUpdatePhase).append(", retryCount=")
                        .append(retryCount).append("]");
        return builder.toString();
    }

   
}
