package com.fedex.peripherals.installer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.foundation.PeripheralAction;
import com.fedex.peripherals.softwareupdate.foundation.PeripheralProperty;
import com.fedex.peripherals.softwareupdate.utilities.PeripheralSoftwareUpdateUtilities;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for loading the action in peripheral server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralSoftwareActionLoader {

    private static final Logger logger = LogManager.getLogger(PeripheralSoftwareActionLoader.class);

    /**
     * This method return the list of the PeripheralSoftwareUpdateAction having all the action defined in the XML.
     * 
     * @param list
     * @return
     * @return List<PeripheralSoftwareUpdateAction>
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param peripheralSoftwareLoadProperties
     * @param argInstallationPath
     */
    public List<PeripheralSoftwareUpdateAction> buildActions(List<PeripheralAction> list,
                    PeripheralSoftwareLoadProperties peripheralSoftwareLoadProperties, String argInstallationPath) {
        logger.traceEntry("buildActions method of PeripheralSoftwareActionLoader class");
        List<PeripheralSoftwareUpdateAction> peripheralActions = new ArrayList<>();
        PeripheralSoftwareUpdateUtilities peripheralSoftwareUtiity = new PeripheralSoftwareUpdateUtilities();
        try {
            logger.debug("create the object of peripheralsoftwareupdateAction for all the action defined in XML");
            for (PeripheralAction peripheralAction : list) {
                logger.info("Peripheral Action {} , {} has been loaded", peripheralAction.getName(),peripheralAction.getDescription());
                PeripheralSoftwareUpdateAction peripheralsoftwareupdateAction =
                                (PeripheralSoftwareUpdateAction) peripheralSoftwareUtiity
                                                .createClass(peripheralAction.getClassName());
                peripheralsoftwareupdateAction.setName(peripheralAction.getName());
                peripheralsoftwareupdateAction.setDescription(peripheralAction.getDescription());
                peripheralsoftwareupdateAction.setRollback(peripheralAction.isRollback());
                peripheralsoftwareupdateAction.setActive(peripheralAction.isActive());
                peripheralsoftwareupdateAction.setExitOnFail(peripheralAction.isExitOfFail());
                peripheralsoftwareupdateAction.setInstallationPath(argInstallationPath);
                peripheralsoftwareupdateAction.setProps(buildPropertiesForAction(peripheralAction.getProperty(),
                                peripheralSoftwareLoadProperties.getProperty()));
                peripheralsoftwareupdateAction
                                .setPeripheralSoftwareUpdatePhase(peripheralAction.getPeripheralSoftwareUpdatePhase());
                peripheralsoftwareupdateAction.setRetryCount(peripheralAction.getRetryCount());
                peripheralActions.add(peripheralsoftwareupdateAction);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new PeripheralServerInstallerException(
                            PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorCode(),
                            exception.getMessage(), exception.getCause());
        }
        logger.traceExit();
        return peripheralActions;
    }

    /**
     * This method is used to build properties for the action
     * 
     * @param property
     * @return
     * @return Properties
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param properties
     */
    public Properties buildPropertiesForAction(List<PeripheralProperty> property, Properties properties) {
        logger.traceEntry("buildPropertiesForAction method of PeripheralSoftwareActionLoader class");
        Properties props = new Properties();
        property = property != null ? property : Collections.EMPTY_LIST;
        for (PeripheralProperty peripheralProperty : property) {
            if (peripheralProperty.getPropvalue().contains("$")) {
                // substring the first character from the property value
                props.put(peripheralProperty.getPropname(), resolveProperty(properties,peripheralProperty.getPropvalue()));
            } else {
                props.put(peripheralProperty.getPropname(), peripheralProperty.getPropvalue());
            }

        }
        Set<Object> propKeys = properties.keySet();
        for (Object prop : propKeys) {
            props.put(prop,resolveProperty(properties, properties.getProperty((String)prop)));
        }
        logger.traceExit();
        return props;
    }

    private String resolveProperty(Properties properties, String propertyKey) {
        while (propertyKey.contains("$")) {
            String referencedProperty = propertyKey.substring(propertyKey.indexOf("${") + 2, propertyKey.indexOf('}'));
            propertyKey = propertyKey.replace("${" + referencedProperty + '}',
                            properties.getProperty(referencedProperty));
        }
        return propertyKey;
    }
}
