package com.fedex.peripherals.installer.actions;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

public class PeripheralSearchAndCopyFileAction extends PeripheralCopyAction {

    private String fileName;
    private String fileExtension;
    private String sourceLocation;
    private static final Logger logger = LogManager.getLogger(PeripheralSearchAndCopyFileAction.class);

    @Override
    protected void init() {
        super.init();
        fileName = (String) getProps().get(PeripheralSoftwareUpdateConstants.FILE_SEARCH_TERM);
        fileExtension = (String) getProps().get(PeripheralSoftwareUpdateConstants.FILE_SEARCH_EXTENSION);
        sourceLocation = (String) getProps().get(PeripheralSoftwareUpdateConstants.FILE_SRC_PATH);
        source = getFileLocation();
    }

    /**
     * This method return the file full path
     * 
     * @return
     * @since Oct 16, 2019
     * @version 0.0.1
     */
    private String getFileLocation() {

        File files = new File(sourceLocation);
        if (files.isDirectory()) {
            File[] fileList = files.listFiles();
            for (File file : fileList) {
                if (file.getName().contains(fileName) && file.getName().contains(fileExtension)) {
                    sourceLocation = sourceLocation.concat(file.getName());
                    break;
                }
            }
        } else {
            logger.error("Location is not a directory {}", sourceLocation);
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Location is not a directory" + sourceLocation, null);
        }
        return sourceLocation;
    }
}
