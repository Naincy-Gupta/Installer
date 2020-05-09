package com.fedex.peripherals.installer;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for loading the peripheralServerInstaller.properties file
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 16-Oct-2019
 */
public class PeripheralSoftwareLoadProperties {

    private static final Logger logger = LogManager.getLogger(PeripheralSoftwareLoadProperties.class);

    private Properties property = new Properties();

    /**
     * @param configPropertyPath
     */
    public PeripheralSoftwareLoadProperties(String configPropertyPath) {
        loadConfigProperties(configPropertyPath);
    }

    /**
     *  Default constructor
     */
    public PeripheralSoftwareLoadProperties() {}

    /**
     * @param configPropertyPath
     */
    private void loadConfigProperties(String configPropertyPath) {
        try(FileInputStream propsIn = new FileInputStream(configPropertyPath)) {
            property.load(propsIn);
        } catch (Exception exception) {
            logger.error(PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorMessage(),
                            ExceptionUtils.getStackTrace(exception));
            throw new PeripheralServerInstallerException(
                            PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorCode(),
                            "Error Occurred while loading peripheral config properties", exception);
        } 
    }

    /**
     * @return
     */
    public Properties getProperty() {
        return property;
    }

    /**
     * @param property
     */
    public void setProperty(Properties property) {
        this.property = property;
    }
}
