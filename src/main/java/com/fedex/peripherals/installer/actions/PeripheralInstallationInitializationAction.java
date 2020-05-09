package com.fedex.peripherals.installer.actions;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br>
 * <br>
 * 
 * Theme - Core Retail Peripheral Services<br>
 * Feature - Peripheral Services - Implement application performance monitoring<br>
 * Description - This is the Action class used for initializing the peripheral-server installation process
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 17-Oct-2019
 */
public class PeripheralInstallationInitializationAction extends PeripheralSoftwareUpdateAction {

    private String serviceId;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private String xmlDestPath;
    private String jarLocation;
    private String fullBuildXmlLocInJar;
    private String incrementalBuildXmlLocInJar;
    private String jarName;
    private String configFileLocation;
    PeripheralCopyResourceFromJarAction peripheralCopyResourceFromJarAction;

    private static final String JAR_LOC_PROP = "jarLoc";
    private static final String DLL_LOCINJAR_PROP = "dllLocInJar";
    private static final String DEST_PATH_PROP = "destPath";
    private static final String ZIPPED_LOC_PROP = "zippedLoc";
    private static final String JAR_NAME_PROP = "jarName";

    private FileSystem fileSystem = FileSystems.getDefault();
    private static final Logger logger = LogManager.getLogger(PeripheralInstallationInitializationAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        serviceId = (String) getProps().get(PeripheralSoftwareUpdateConstants.SERVICE_ID);
        xmlDestPath = (String) getProps().get(PeripheralSoftwareUpdateConstants.XML_DEST_PATH);
        jarLocation = (String) getProps().get(PeripheralSoftwareUpdateConstants.JAR_LOCATION);
        jarName = (String) getProps().get(PeripheralSoftwareUpdateConstants.JAR_NAME);
        fullBuildXmlLocInJar = (String) getProps().get(PeripheralSoftwareUpdateConstants.FULLBUILD_XML_LOC_IN_JAR);
        incrementalBuildXmlLocInJar =
                        (String) getProps().get(PeripheralSoftwareUpdateConstants.INCREMENTAL_XML_LOC_IN_JAR);
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        peripheralCopyResourceFromJarAction = new PeripheralCopyResourceFromJarAction();
    }

    /**
     * This method execute action to determine whether full build or incremental build is required
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralInstallationInitializationAction class");
        init();
        logger.info("Checking if peripheral-server is already installed to determine whether full build or incremental build is required");

        String serviceStatus;
        int statusCode = installationPhase.getErrorCode();

        PeripheralServerServiceStatusAction peripheralServerServiceStatusAction =
                        new PeripheralServerServiceStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        peripheralServerServiceStatusAction.setInstallationPhase(installationPhase);
        peripheralServerServiceStatusAction.setProps(properties);
        peripheralServerServiceStatusAction.execute();

        serviceStatus = peripheralServerServiceStatusAction.getServiceStatus();

        if (serviceStatus == null) {

            Properties props = new Properties();
            props.setProperty(JAR_LOC_PROP, jarLocation);
            props.setProperty(DLL_LOCINJAR_PROP, fullBuildXmlLocInJar);
            props.setProperty(DEST_PATH_PROP, xmlDestPath);
            props.setProperty(ZIPPED_LOC_PROP, xmlDestPath);
            props.setProperty(JAR_NAME_PROP, jarName);
            peripheralCopyResourceFromJarAction.setProps(props);
            peripheralCopyResourceFromJarAction.setPeripheralSoftwareUpdatePhase(installationPhase);
            peripheralCopyResourceFromJarAction.execute();
            statusCode = PeripheralSoftwareUpdateConstants.FULL_INSTALLATION_BUILD;
            PeripheralPropertyFileUpdateAction peripheralPropertyFileUpdateAction =
                            new PeripheralPropertyFileUpdateAction();
            Properties propertiesToUpdateConfigPropertyFile = new Properties();
            propertiesToUpdateConfigPropertyFile.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_FILE_LOCATION,
                            configFileLocation);
            propertiesToUpdateConfigPropertyFile.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_NAME,
                            PeripheralSoftwareInstallerPropertyConstants.INSTALLATION_LOCATION);
            propertiesToUpdateConfigPropertyFile.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_VALUE,
                            getInstallationPath());
            peripheralPropertyFileUpdateAction.setProps(propertiesToUpdateConfigPropertyFile);
            peripheralPropertyFileUpdateAction.execute();
            logger.info("Peripheral server is not installed on the system Full Build will be triggered");
        } else if (serviceStatus.contains("RUNNING") || serviceStatus.contains("STOPPED")) {
            Properties props = new Properties();
            props.setProperty(JAR_LOC_PROP, jarLocation);
            props.setProperty(DLL_LOCINJAR_PROP, incrementalBuildXmlLocInJar);
            props.setProperty(ZIPPED_LOC_PROP, xmlDestPath);
            props.setProperty(JAR_NAME_PROP, jarName);
            peripheralCopyResourceFromJarAction.setProps(props);
            peripheralCopyResourceFromJarAction.setPeripheralSoftwareUpdatePhase(installationPhase);
            peripheralCopyResourceFromJarAction.execute();
            statusCode = PeripheralSoftwareUpdateConstants.INCREMENTAL_INSTALLATION_BUILD;
            PeripheralReadEnviromentVariableAction peripheralReadEnviromentVariableAction =
                            new PeripheralReadEnviromentVariableAction();
            Properties readEnviromentVariableProperties = new Properties();
            readEnviromentVariableProperties.setProperty(
                            PeripheralSoftwareUpdateConstants.PERIPHERAL_INSTALLER_ENVIROMENT_VARIABLE,
                            PeripheralSoftwareUpdateConstants.PERIPHERAL_INSTALLER_LOCATION_ENVIROMENT_VARIABLE_NAME);
            peripheralReadEnviromentVariableAction.setProps(readEnviromentVariableProperties);
            peripheralReadEnviromentVariableAction.execute();

            PeripheralPropertyFileUpdateAction peripheralPropertyFileUpdateAction =
                            new PeripheralPropertyFileUpdateAction();
            Properties propertiesToUpdateConfigPropertyFile = new Properties();
            propertiesToUpdateConfigPropertyFile.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_FILE_LOCATION,
                            configFileLocation);
            propertiesToUpdateConfigPropertyFile.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_NAME,
                            PeripheralSoftwareInstallerPropertyConstants.INSTALLATION_LOCATION);
            propertiesToUpdateConfigPropertyFile.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_VALUE,
                            peripheralReadEnviromentVariableAction.getEnviromentVariableValue());
            peripheralPropertyFileUpdateAction.setProps(propertiesToUpdateConfigPropertyFile);
            peripheralPropertyFileUpdateAction.execute();
            logger.info("Peripheral server is installed on the system Incremental Build will be triggered");
        }
        return logger.traceExit(statusCode);
    }

    /**
     * This method cleans up the intermediate file/folders created while performing
     * PeripheralInstallationInitializationAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of PeripheralInstallationInitializationAction class");
        Properties props = new Properties();
        props.setProperty(JAR_LOC_PROP, jarLocation);
        props.setProperty(DLL_LOCINJAR_PROP, fullBuildXmlLocInJar);
        props.setProperty(DEST_PATH_PROP, xmlDestPath);
        props.setProperty(ZIPPED_LOC_PROP, xmlDestPath);
        props.setProperty(JAR_NAME_PROP, jarName);
        peripheralCopyResourceFromJarAction.setProps(props);
        peripheralCopyResourceFromJarAction.setPeripheralSoftwareUpdatePhase(PeripheralSoftwarePhasesErrorCode.CLEANUP);
        peripheralCopyResourceFromJarAction.cleanUp();
        return logger.traceExit(0);
    }

    /**
     * This method undo the PeripheralInstallationInitializationAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method perform the validation of PeripheralInstallationInitializationAction
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralInstallationInitializationAction class");
        Path xmlDestinationPath = fileSystem.getPath(xmlDestPath);
        logger.debug("Checking destination file {} exists", xmlDestPath);
        return logger.traceExit(xmlDestinationPath.toFile().exists());
    }

    public String getConfigFileLocation() {
        return configFileLocation;
    }

    public void setConfigFileLocation(String configFileLocation) {
        this.configFileLocation = configFileLocation;
    }

}
