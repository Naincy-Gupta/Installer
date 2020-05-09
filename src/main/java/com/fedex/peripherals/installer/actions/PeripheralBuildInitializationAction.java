package com.fedex.peripherals.installer.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralBuildType;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for creating the backup of folder based on build type
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralBuildInitializationAction extends PeripheralSoftwareUpdateAction {

    private String buildType;
    private int countFileExits = 0;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private Map<File, File> backupFileList;
    private PeripheralCopyAction peripheralCopyAction;
    private PeripheralDeleteAction peripheralDeleteAction;
    private PeripheralServerStopAction peripheralServerStopAction;
    private Properties properties,stopActionProperties;
    private PeripheralServerStartAction peripheralServerStartAction;
    private String serviceId;
    private boolean serviceStop;
    private static final Logger logger = LogManager.getLogger(PeripheralBuildInitializationAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void init() {
        buildType = (String) getProps().get(PeripheralSoftwareUpdateConstants.BUILD_TYPE);
        serviceId = (String) getProps().get(PeripheralSoftwareUpdateConstants.SERVICE_ID);
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        peripheralCopyAction = new PeripheralCopyAction();
        peripheralDeleteAction = new PeripheralDeleteAction();
        peripheralServerStopAction = new PeripheralServerStopAction();
        properties = getProps();
        peripheralServerStartAction = new PeripheralServerStartAction();
    }

    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralBuildInitializationAction class");
        try {
            init();
            /*
             * If build type is incremental, then stop the service if the status of the service is running and create backup for files and folders whose
             * location is present in propertyconfig file
             */
            if (StringUtils.equalsIgnoreCase(PeripheralBuildType.INCREMENTBUILD.name(), buildType)) {
                // checking the status of the installed service
                PeripheralServerServiceStatusAction peripheralServerServiceStatusAction =
                                new PeripheralServerServiceStatusAction();
                stopActionProperties = new Properties();
                stopActionProperties.setProperty("serviceId", serviceId);
                peripheralServerServiceStatusAction.setProps(stopActionProperties);
                peripheralServerServiceStatusAction.setInstallationPhase(installationPhase);
                peripheralServerServiceStatusAction.execute();
                if (peripheralServerServiceStatusAction.getServiceStatus() != null
                                && peripheralServerServiceStatusAction.getServiceStatus().contains("RUNNING")) {
                    // service stop parameter is set to true if the already installed service is in running state.
                    serviceStop = true;
                    // get properties from external installer property location
                    peripheralServerStopAction.setPeripheralSoftwareUpdatePhase(installationPhase);
                    properties.setProperty(PeripheralSoftwareUpdateConstants.WINSW_EXE_LOC, properties
                                    .getProperty(PeripheralSoftwareInstallerPropertyConstants.EXECUTABLE_JAR_LOCATION));
                    properties.setProperty(PeripheralSoftwareUpdateConstants.SERVICE_ID, serviceId);
                    peripheralServerStopAction.setProps(properties);
                    peripheralServerStopAction.execute();
                }
                createBackup();
            }
        } catch (Exception exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                            exception.getCause());
        }
        return logger.traceExit(0);
    }

    /**
     * This method created the backup of all the file and folder whose location is present in property file
     * 
     * @throws ConfigurationException
     * @return void
     * @since Oct 14, 2019
     * @version 0.0.1
     */
    private void createBackup() {
        logger.traceEntry("createBackup method of PeripheralBuildInitializationAction class");
        backupFileList = new HashMap<>();
        String propertiesValue =
                        properties.getProperty(PeripheralSoftwareInstallerPropertyConstants.FILE_FOLDER_BACKUP_LIST);
        List<String> propertyValue = Arrays.asList(propertiesValue.split(","));
        propertyValue.forEach(fileLoc -> {
            File sourceFile = new File(fileLoc);
            if (sourceFile.exists()) {
                if (sourceFile.isDirectory()) {
                    createFolderBackup(sourceFile);
                } else {
                    createFileBackup(sourceFile);
                }

            } else {
                logger.error("Source file doesn't exists at defined location {} ", sourceFile);
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                "Source file not exists " + sourceFile, null);
            }

        });
        logger.trace("Exit createBackup()");
    }

    private void createFileBackup(File sourceFile) {
        logger.traceEntry("createFileBackup method of PeripheralBuildInitializationAction class");
        File backUpFile = new File(sourceFile.getPath() + "_bckp_" + new Date().getTime());
        if (sourceFile.renameTo(backUpFile)) {
            backupFileList.put(backUpFile, sourceFile);
        } else {
            logger.error("Cannot create backup for source {}", sourceFile);
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Can't create the backup " + sourceFile, null);
        }
        logger.trace("Exit createFileBackup()");
    }

    private void createFolderBackup(File sourceFile) {
        logger.traceEntry("createFolderBackup method of PeripheralBuildInitializationAction class");
        try {
            String backupFileName = sourceFile.getPath() + "_bckp_" + new Date().getTime();
            Files.move(sourceFile.toPath(), sourceFile.toPath().resolveSibling(backupFileName));
            backupFileList.put(new File(backupFileName), sourceFile);
        } catch (IOException ioException) {
            logger.error("Cannot create backup for source {}", sourceFile);
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Can't create the backup " + sourceFile, null);
        }
        logger.trace("Exit createFolderBackup()");
    }


    /**
     * This method used to perform undo action of PeripheralBuildInstallationAction
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */

    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        logger.traceEntry("undoPeripheralSoftwareUpdateAction method of PeripheralBuildInitializationAction class");
        if (StringUtils.equalsIgnoreCase(PeripheralBuildType.INCREMENTBUILD.name(), buildType)) {
            if (backupFileList != null) {
                backupFileList.forEach((destBackUpFile, sourceFileToBeBackuped) -> {
                    if (destBackUpFile.exists() && destBackUpFile.isDirectory()) {
                        properties.setProperty(PeripheralSoftwareUpdateConstants.COPY_SRC_PATH,
                                        destBackUpFile.toString());
                        properties.setProperty(PeripheralSoftwareUpdateConstants.COPY_DEST_PATH,
                                        sourceFileToBeBackuped.toString());
                        properties.setProperty(PeripheralSoftwareUpdateConstants.COPY_CREATE_DIR_NOT_PRESENT, "true");
                        properties.setProperty(PeripheralSoftwareUpdateConstants.DELETE_SOURCE_DIRECTORY, "false");
                        properties.setProperty(PeripheralSoftwareUpdateConstants.BACKUP_DESTINATION, "false");
                        peripheralCopyAction.setPeripheralSoftwareUpdatePhase(installationPhase);
                        peripheralCopyAction.setProps(properties);
                        peripheralCopyAction.execute();
                        peripheralDeleteAction.setPeripheralSoftwareUpdatePhase(installationPhase);
                        properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH,
                                        destBackUpFile.toString());
                        peripheralDeleteAction.setProps(properties);
                        peripheralDeleteAction.execute();
                    } else if (destBackUpFile.exists() && destBackUpFile.isFile()
                                    && destBackUpFile.renameTo(sourceFileToBeBackuped)) {
                        logger.debug("Source file rename to {} ", sourceFileToBeBackuped);
                    } else {
                        logger.error("Source file not exists {} ", destBackUpFile);
                    }

                });
            }
            // based on service stop parameter it is starting the service
            if (serviceStop) {
                peripheralServerStartAction.setPeripheralSoftwareUpdatePhase(installationPhase);
                properties.setProperty(PeripheralSoftwareUpdateConstants.WINSW_EXE_LOC, properties
                                .getProperty(PeripheralSoftwareInstallerPropertyConstants.EXECUTABLE_JAR_LOCATION));
                properties.setProperty(PeripheralSoftwareUpdateConstants.SERVICE_ID, serviceId);
                peripheralServerStartAction.setInstallationPath(getInstallationPath());
                peripheralServerStartAction.setProps(properties);
                peripheralServerStartAction.execute();
            }
        }
        return logger.traceExit(0);
    }

    /**
     * This method used to perform validation Action PeripheralBuildInstallationAction
     * 
     * @return
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralBuildInitializationAction class");
        if (StringUtils.equalsIgnoreCase(PeripheralBuildType.INCREMENTBUILD.name(), buildType)) {
            backupFileList.forEach((destBackUpFile, sourceFileToBeBackuped) -> {
                if (destBackUpFile.exists()) {
                    countFileExits++;
                } else {
                    logger.error("Source file doesn't exists {} ", destBackUpFile);
                }

            });

            return logger.traceExit(backupFileList.size() == countFileExits);
        }
        return logger.traceExit(true);
    }

    /**
     * This method perform the cleanUp Action
     * 
     * @return
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of PeripheralBuildInitializationAction class");
        // only do clean up when it is an incremental build
        if (StringUtils.equalsIgnoreCase(PeripheralBuildType.INCREMENTBUILD.name(), buildType)
                        && backupFileList != null) {
            backupFileList.forEach((destBackUpFile, sourceFileToBeBackuped) -> {
                if (destBackUpFile.exists()) {
                    peripheralDeleteAction.setPeripheralSoftwareUpdatePhase(PeripheralSoftwarePhasesErrorCode.CLEANUP);
                    properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH,
                                    destBackUpFile.toString());
                    peripheralDeleteAction.setProps(properties);
                    peripheralDeleteAction.execute();
                } else {
                    logger.error("Source file doesn't exists {} ", destBackUpFile);
                }

            });
        }
        return logger.traceExit(0);

    }


}
