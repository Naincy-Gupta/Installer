package com.fedex.peripherals.installer.actions;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This Action class is used to perform the delete action on any file or folder
 * 
 * @author Shagun Sharma [3696362]
 * @version 0.0.1
 * @since Oct 10, 2019
 */
public class PeripheralDeleteAction extends PeripheralSoftwareUpdateAction {

    protected String sourcePath;
    protected File sourceFile;
    protected File sourceFileBackup;
    protected boolean createBackup;
    protected boolean isFileToBeDeletedPresent;
    protected PeripheralSoftwarePhasesErrorCode installationPhase;
    private static final Logger logger = LogManager.getLogger(PeripheralDeleteAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    protected void init() {
        sourcePath = (String) getProps().get(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH);
        createBackup = Boolean.valueOf((String) getProps().get(PeripheralSoftwareUpdateConstants.CREATE_BACKUP));
        isFileToBeDeletedPresent = false;
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
    }

    /**
     * This method execute the PeripheralDeleteAction action
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralDeleteAction class");
        init();
        String newFileNameAppender = "_bckp_";
        try {
            sourceFile = new File(sourcePath);
            if (createBackup) {
                sourceFileBackup = new File(sourcePath + newFileNameAppender + new Date().getTime());
                createDir(sourceFile, sourceFileBackup);
            }
            if (sourceFile.exists()) {
                isFileToBeDeletedPresent = true;
                FileUtils.forceDelete(sourceFile);
            } else {
                logger.error("The file to be deleted does not exist {}", sourceFile);
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "could not delete file " + sourceFile, ioException.getCause());
        }
        return logger.traceExit(0);
    }

    /**
     * This method used to create Directory
     * 
     * @param sourceFile
     * @param destinationFile
     * @return void
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    protected void createDir(File sourceFile, File destinationFile) {
        try {
            FileUtils.forceMkdir(destinationFile);
            FileUtils.copyDirectory(sourceFile, destinationFile);
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "could not create a backup of files before deleting " + sourceFile, ioException.getCause());
        }
    }

    /**
     * This method used to perform undo action of PeripheralDeleteAction
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        logger.traceEntry("undoPeripheralSoftwareUpdateAction method of PeripheralDeleteAction class");
        if (sourceFile.exists()) {
            createDir(sourceFileBackup, sourceFile);
        } else {
            logger.error("Source file doesn't exists {}", sourceFile);
        }
        return logger.traceExit(0);
    }

    /**
     * This method used to perform the validation action of PeripheralDeleteAction
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralDeleteAction class");
        if (!isFileToBeDeletedPresent) {
            logger.error("The file to be deleted did not exist in first place");
            return logger.traceExit(true);
        } else if (!sourceFile.exists()) {
            logger.debug("File deleted successfully {}", sourceFile);
            return logger.traceExit(true);
        } else {
            logger.debug("The file could not be deleted {}", sourceFile);
            return logger.traceExit(false);
        }
    }

    /**
     * This method used to perform the cleanUp
     * 
     * @return
     * @since Oct 10, 2019
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of PeripheralDeleteAction class");
        try {
            if ((null != sourceFileBackup) && sourceFileBackup.exists()) {
                FileUtils.forceDelete(sourceFileBackup);
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "could not delete the backup files " + sourceFileBackup, ioException.getCause());
        }
        return logger.traceExit(0);
    }

}
