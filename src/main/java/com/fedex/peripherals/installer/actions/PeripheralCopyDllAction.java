/**
 * 
 */
package com.fedex.peripherals.installer.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for copying device dll to provided destination
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 29-Oct-2019
 */
public class PeripheralCopyDllAction extends PeripheralCopyAction {

    private static final Logger logger = LogManager.getLogger(PeripheralCopyDllAction.class);
    private PeripheralDeleteAction deleteDllAction;

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 29, 2019
     * @version 0.0.1
     */
    @Override
    protected void init() {
        logger.info("Calling init method of PeripheralCopyDllAction class");
        super.init();
        deleteDllAction = new PeripheralDeleteAction();
        source = (String) getProps().get(PeripheralSoftwareUpdateConstants.COPY_SRC_PATH);
        destination = (String) getProps().get(PeripheralSoftwareUpdateConstants.COPY_DEST_PATH);
    }

    /**
     * This method execute the Copy dll Action
     * 
     * @return
     * @since Apr 14, 2020
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralCopyDllAction class");
        init();
        String newFileNameAppender = "_bckp_";
        sourceFile = new File(source);
        destFile = new File(destination);
        try {
            if (!sourceFile.exists()) {
                String directorySpilitter = "\\";
                // Fetching name of the  Parent directory in which the dll file is present
                String rootDirectory = source.substring(0, source.lastIndexOf(directorySpilitter));
                // Fetching the name of the file provided in the xml
                String childDirectory = source.substring(source.lastIndexOf(directorySpilitter) + 1, source.length());
                File folder = new File(rootDirectory);
                File[] fileList = folder.listFiles();
                for (File file : fileList) {
                    // Comparing the name provided in the xml with the list of files present in the parent directory
                    if (file.getName().startsWith(childDirectory)) {
                        sourceFile = new File(rootDirectory + file.getName());
                        break;
                    }
                }
            }
            if (sourceFile.canRead()) {
                if (destFile.exists()) {
                    if (destFile.isDirectory() && backUpDestinationDirectoryCreatedIfAlreadyExixts) {
                        Files.move(destFile.toPath(), destFile.toPath()
                                        .resolveSibling(destFile + newFileNameAppender + new Date().getTime()));
                        wasDestinationDirectoryCreated = true;
                        FileUtils.forceMkdir(destFile);
                        return logger.traceExit(createBackUpFile(newFileNameAppender));
                    } else {
                        return logger.traceExit(createBackUpFile(newFileNameAppender));
                    }
                } else if (createDestinationIfNotPresent) {
                    logger.debug("Creating the directory if directory is not present");
                    return logger.traceExit(createDirectory());
                } else {
                    logger.error("destination does not exist and create destination is false {}", destFile);
                    throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                    "destination does not exist and create destination is false " + destFile, null);
                }
            } else {
                logger.error("unreadable source {} ", sourceFile);
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                "unreadable source " + sourceFile, null);
            }

        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        }
    }

    /**
     * This method perform the cleanUp action
     * 
     * @return
     * @since Apr 14, 2020
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of PeripheralCopyDllAction class");
        deleteDllAction.setPeripheralSoftwareUpdatePhase(PeripheralSoftwarePhasesErrorCode.CLEANUP);
        if (deleteSourceDirectory && sourceFile.exists()) {
            logger.debug("Creating the object of properties object and setting the value");
            Properties properties = new Properties();
            properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH, sourceFile.toString());
            properties.setProperty(PeripheralSoftwareUpdateConstants.CREATE_BACKUP, "false");
            deleteDllAction.setProps(properties);
            logger.debug("Calling  the execute method of delete action");
            deleteDllAction.execute();
        }
        return logger.traceExit(0);
    }

}
