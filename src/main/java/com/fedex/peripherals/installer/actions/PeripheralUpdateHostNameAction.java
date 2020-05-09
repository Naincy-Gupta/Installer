package com.fedex.peripherals.installer.actions;

import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;
import com.fedex.peripherals.softwareupdate.utilities.PeripheralSoftwareUpdateUtilities;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for updating host name in the Property file
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralUpdateHostNameAction extends PeripheralSoftwareUpdateAction {

    private String source;
    private String propertyName;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private PeripheralPropertyFileUpdateAction peripheralPropertyFileUpdateAction;
    private static final Logger logger = LogManager.getLogger(PeripheralUpdateHostNameAction.class);
    PeripheralSoftwareUpdateUtilities peripheralSoftwareUpdateUtilities;

    public void init() {
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        peripheralPropertyFileUpdateAction = new PeripheralPropertyFileUpdateAction();
        peripheralSoftwareUpdateUtilities = new PeripheralSoftwareUpdateUtilities();
        source = (String) getProps().get(PeripheralSoftwareUpdateConstants.PROPERTY_FILE_LOCATION);
        propertyName = (String) getProps().get(PeripheralSoftwareUpdateConstants.PROPERTY_NAME);
    }

    /**
     * This method execute the PeripheralUpdateHostNameAction Action
     * 
     * @param fullClassName
     * @return Object
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralUpdateHostNameAction class");
        init();
        peripheralPropertyFileUpdateAction.setPeripheralSoftwareUpdatePhase(installationPhase);
        logger.debug("Creating the object of peripheralPropertyFileUpdateAction");
        Properties properties = new Properties();
        logger.debug("Setting the property value");
        properties.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_FILE_LOCATION, source);
        properties.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_NAME, propertyName);
        try {
            properties.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_VALUE,
                            peripheralSoftwareUpdateUtilities.getPeripheralServerHostName());
        } catch (UnknownHostException unknownHostException) {
            logger.error("An exception while fetching hostname has occured {}", unknownHostException.getMessage());
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            unknownHostException.getMessage(), unknownHostException.getCause());
        }

        peripheralPropertyFileUpdateAction.setProps(properties);
        logger.debug("Calling  the execute method of delete action");
        peripheralPropertyFileUpdateAction.execute();
        return logger.traceExit(0);
    }

    /**
     * This method is the undo the PeripheralUpdateHostNameAction Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        logger.debug("calling the undo action of PeripheralUpdateHostNameAction class");
        return peripheralPropertyFileUpdateAction.undoPeripheralSoftwareUpdateAction();
    }

    /**
     * This method is the validateAction the PeripheralUpdateHostNameAction Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.debug("calling the  validateAction of PeripheralUpdateHostNameAction class");
        return peripheralPropertyFileUpdateAction.validateAction();
    }

    /**
     * This method is the cleanUp the PeripheralUpdateHostNameAction Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        return 0;
    }

}
