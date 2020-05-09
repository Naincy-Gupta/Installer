package com.fedex.peripherals.installer.actions;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
 * Description - This is the Action class used for Unzipping the Zipped file
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralUnZippedAction extends PeripheralSoftwareUpdateAction {

    private String destinaltionDllZippedFileLocation;
    private String unZippedParentDirectoryLocation;
    private String unZippedDirectory;
    private boolean wasDestinationDirectoryCreated;
    private boolean wasDestinationParentDirectoryCreated;
    private FileSystem fileSystem = FileSystems.getDefault();
    private PeripheralDeleteAction deleteAction;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private static final Logger logger = LogManager.getLogger(PeripheralUnZippedAction.class);

    /**
     * This method initialize the instance variable through by reading the value from XML file
     * 
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void init() {
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        deleteAction = new PeripheralDeleteAction();
        // get unzip location from installer path
        destinaltionDllZippedFileLocation =(String) getProps().get(PeripheralSoftwareUpdateConstants.DEST_ZIPPED_LOC);
        unZippedParentDirectoryLocation = (String) getProps().get(PeripheralSoftwareUpdateConstants.DEST_UNZIPPED_LOC);
    }

    /**
     * This method execute the unZipped Action
     * 
     * @return int
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralUnZippedAction class");
        try {
            init();
            Path uncompressedDirectoryPath = fileSystem.getPath(unZippedParentDirectoryLocation);
            if (!uncompressedDirectoryPath.toFile().exists()) {
                Files.createDirectories(uncompressedDirectoryPath);
                wasDestinationParentDirectoryCreated = true;
            }
            try (ZipFile zipFile = new ZipFile(destinaltionDllZippedFileLocation)) {
                // Get file entries
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                // Iterate over entries
                unZip(zipFile, entries, unZippedParentDirectoryLocation);
            }
            logger.debug("Deleting the file {}", destinaltionDllZippedFileLocation);
            Path zipedFileLocationPath = fileSystem.getPath(destinaltionDllZippedFileLocation);
            Files.deleteIfExists(zipedFileLocationPath);

        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        }
        return logger.traceExit(0);
    }

    /**
     * This method unzipped the file and copy all the content to destination folder
     * 
     * @param zipFile
     * @param entries
     * @param uncompressedDirectory
     * @throws IOException
     * @throws FileNotFoundException
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void unZip(ZipFile zipFile, Enumeration<? extends ZipEntry> entries, String uncompressedDirectory)
                    throws IOException {
        logger.traceEntry("unZip method of PeripheralUnZippedAction class");
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            // If directory then create a new directory in uncompressed folder
            if (entry.isDirectory()) {
                unZippedDirectory = uncompressedDirectory + entry.getName();
                Path unZippedDirectoryPath = fileSystem.getPath(unZippedDirectory);
                if (unZippedDirectoryPath.toFile().exists()) {
                    String newFileNameAppender = "_bckp_";
                    Files.move(unZippedDirectoryPath, unZippedDirectoryPath.resolveSibling(
                                    unZippedDirectoryPath + newFileNameAppender + new Date().getTime()));
                    Files.createDirectories(unZippedDirectoryPath);
                    wasDestinationDirectoryCreated = true;
                } else {
                    Files.createDirectories(unZippedDirectoryPath);
                    wasDestinationDirectoryCreated = true;
                }
            }
            // Else create the file
            else {
                InputStream is = zipFile.getInputStream(entry);
                BufferedInputStream bis = new BufferedInputStream(is);
                String uncompressedFileName = uncompressedDirectory + entry.getName();
                Path uncompressedFilePath = fileSystem.getPath(uncompressedFileName);
                if (!uncompressedFilePath.toFile().exists()) {
                    Files.createFile(uncompressedFilePath);
                }
                try (FileOutputStream fileOutput = new FileOutputStream(uncompressedFileName)) {
                    while (bis.available() > 0) {
                        fileOutput.write(bis.read());
                    }
                }
            }
        }
        logger.trace("Exit unZip()");
    }


    /**
     * This method is the undo the unZipped Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        logger.traceEntry("undoPeripheralSoftwareUpdateAction method of PeripheralUnZippedAction class");
        deleteAction.setPeripheralSoftwareUpdatePhase(installationPhase);
        logger.debug("Creating the object of properties object and setting the value");
        Properties properties = new Properties();
        logger.info("Calls the undoAction method of PeripheralUnZippedAction class");
        if (wasDestinationDirectoryCreated) {
            logger.debug("Deleting the {} Directory when destination directory is created", unZippedDirectory);
            logger.debug("Calling  the execute method of delete action");
            properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH, unZippedDirectory);
            properties.setProperty(PeripheralSoftwareUpdateConstants.CREATE_BACKUP, "false");
            deleteAction.setProps(properties);
            deleteAction.execute();
        }
        if (wasDestinationParentDirectoryCreated) {
            logger.debug("Deleting the {} Directory when destination directory is created",
                            unZippedParentDirectoryLocation);
            logger.debug("Calling  the execute method of delete action");
            properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH,
                            unZippedParentDirectoryLocation);
            properties.setProperty(PeripheralSoftwareUpdateConstants.CREATE_BACKUP, "false");
            deleteAction.setProps(properties);
            deleteAction.execute();
        }
        return logger.traceExit(0);
    }

    /**
     * This method perform the validation of the unzipped action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralUnZippedAction class");
        Path unZippedDirectoryPath = fileSystem.getPath(unZippedDirectory);
        logger.debug("Checking {} file exists or not ", unZippedDirectoryPath);
        return logger.traceExit(unZippedDirectoryPath.toFile().exists());
    }

    @Override
    public int cleanUp() {
        return 0;
    }
}


