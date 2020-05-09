package com.fedex.peripherals.installer.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
 * Description - This is the Action class used for copying the Zipped Dll from the jar present to predefined folder
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralCopyResourceFromJarAction extends PeripheralSoftwareUpdateAction {

    private String jarLocation;
    private String dllFileLocationInJar;
    private String destinaltionDllZippedFileLocation;
    private boolean wasDestinationDirectoryCreated;
    private boolean wasDestinationFileCreated;
    private FileSystem fileSystem = FileSystems.getDefault();
    private PeripheralDeleteAction deleteAction;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private String jarName;
    private ArrayList<String> backUpList;
    private static final Logger logger = LogManager.getLogger(PeripheralCopyResourceFromJarAction.class);


    /**
     * This method initialize the instance variable through by reading the value from XML file
     * 
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void init() {
        deleteAction = new PeripheralDeleteAction();
        jarName = (String) getProps().get(PeripheralSoftwareUpdateConstants.JAR_NAME);
        // if installation path is null, set the jar location from property file
        jarLocation = (String) getProps().get(PeripheralSoftwareUpdateConstants.JAR_LOCATION);
        dllFileLocationInJar = (String) getProps().get(PeripheralSoftwareUpdateConstants.DLL_LOCATION_IN_JAR);
        // set destinaton path according to check for installation path
        destinaltionDllZippedFileLocation = (String) getProps().get(PeripheralSoftwareUpdateConstants.DEST_ZIPPED_LOC);
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
    }

    /**
     * This method execute the Copy Resource From Jar Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralCopyResourceFromJarAction class");
        String newFileNameAppender = "_bckp_";
        init();
        backUpList = new ArrayList<>();
        try (JarFile jarFile = new JarFile(new File(getJarLocation()))) {
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(dllFileLocationInJar) && !entry.isDirectory()) {
                    Path destinaltionDllZippedFile = fileSystem.getPath(destinaltionDllZippedFileLocation);
                    Path destinaltionDllZippedFileParentPath = destinaltionDllZippedFile.getParent();
                    if (!destinaltionDllZippedFileParentPath.toFile().exists()) {
                        logger.debug("creating the {} parent Folder created", destinaltionDllZippedFileParentPath);
                        wasDestinationDirectoryCreated = true;
                        Files.createDirectory(destinaltionDllZippedFileParentPath);
                    }
                    if (!destinaltionDllZippedFile.toFile().exists()) {
                        logger.debug("creating the {} Folder created", destinaltionDllZippedFile);
                        Files.createFile(destinaltionDllZippedFile);
                        wasDestinationFileCreated = true;
                    } else {
                        logger.debug("Renamin the existing folder and creating the {} Folder created",
                                        destinaltionDllZippedFile);
                        String backUpFileName = destinaltionDllZippedFile + newFileNameAppender + new Date().getTime();
                        Files.move(destinaltionDllZippedFile, destinaltionDllZippedFile.resolveSibling(backUpFileName));
                        backUpList.add(backUpFileName);
                        Files.createFile(destinaltionDllZippedFile);
                        wasDestinationFileCreated = true;
                    }
                    copyResouce(jarFile, entry);
                }
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        }
        return logger.traceExit(0);
    }

    /**
     * This method return the jar full path
     * 
     * @return
     * @since Oct 16, 2019
     * @version 0.0.1
     */
    private String getJarLocation() {

        File files = new File(jarLocation);
        if (files.isDirectory()) {
            File[] fileList = files.listFiles();
            for (File file : fileList) {
                if (file.getName().contains(jarName)) {
                    jarLocation = jarLocation.concat(file.getName());
                }
            }
        } else {
            logger.error("Location is not a directory {}", jarLocation);
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Location is not a directory" + jarLocation, null);
        }
        return jarLocation;
    }


    /**
     * This method copy the Resource to predefined location
     * 
     * @param jarFile
     * @param entry
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void copyResouce(JarFile jarFile, JarEntry entry) {
        logger.info("Calls the copyResouce method of PeripheralCopyResourceFromJarAction class");
        try (FileOutputStream out = new FileOutputStream(destinaltionDllZippedFileLocation);
                        InputStream in = jarFile.getInputStream(entry)) {
            logger.debug("Copying the resource to predefined folder");
            byte[] buffer = new byte[8 * 1024];
            int s = 0;
            while ((s = in.read(buffer)) > 0) {
                out.write(buffer, 0, s);
            }
        } catch (IOException ioException) {
            logger.error(ExceptionUtils.getStackTrace(ioException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Copy operation can't be performed", ioException.getCause());
        }
    }

    /**
     * This method undo the unZipped Action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        logger.traceEntry("undoPeripheralSoftwareUpdateAction of PeripheralCopyResourceFromJarAction class");
        deleteAction.setPeripheralSoftwareUpdatePhase(installationPhase);
        logger.debug("Creating the object of properties object and setting the value");
        Properties properties = new Properties();
        Path destinaltionDllZippedFile = null;
        Path destinaltionDllZippedParentFolder = null;
        String createBackUp ="false";

        if (wasDestinationFileCreated) {
            logger.debug("Deleting the {} file created", destinaltionDllZippedFileLocation);
            properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH,
                            destinaltionDllZippedFileLocation);
            properties.setProperty(PeripheralSoftwareUpdateConstants.CREATE_BACKUP, createBackUp);
            deleteAction.setProps(properties);
            logger.debug("Calling  the execute method of delete action");
            deleteAction.execute();
        }
        if (wasDestinationDirectoryCreated && destinaltionDllZippedFileLocation != null) {
            destinaltionDllZippedFile = fileSystem.getPath(destinaltionDllZippedFileLocation);
            destinaltionDllZippedParentFolder = destinaltionDllZippedFile.getParent();
            logger.debug("Deleting the {} file created", destinaltionDllZippedFileLocation);
            properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH,
                            destinaltionDllZippedParentFolder.toString());
            properties.setProperty(PeripheralSoftwareUpdateConstants.CREATE_BACKUP, createBackUp);
            deleteAction.setProps(properties);
            logger.debug("Calling  the execute method of delete action");
            deleteAction.execute();
        }
        return logger.traceExit(0);
    }

    /**
     * This method perform the validation of the copyResourceFromJar action
     * 
     * @return
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralCopyResourceFromJarAction class");
        Path destinaltionDllZippedFile = fileSystem.getPath(destinaltionDllZippedFileLocation);
        logger.debug("Checking destination file {} exixt", destinaltionDllZippedFile);
        return logger.traceExit(destinaltionDllZippedFile.toFile().exists());
    }

    @Override
    public int cleanUp() {
        logger.traceEntry("cleanUp method of PeripheralCopyResourceFromJarAction class");
        if (backUpList != null) {
            backUpList.forEach(backUpFileName -> {
                deleteAction.setPeripheralSoftwareUpdatePhase(installationPhase);
                Properties properties = new Properties();
                properties.setProperty(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH, backUpFileName);
                properties.setProperty(PeripheralSoftwareUpdateConstants.CREATE_BACKUP, "false");
                deleteAction.setProps(properties);
                logger.debug("Calling  the execute method of delete action for deleting {} created as a backup",
                                backUpFileName);
                deleteAction.execute();
            });
        }
        return logger.traceExit(0);
    }

}
