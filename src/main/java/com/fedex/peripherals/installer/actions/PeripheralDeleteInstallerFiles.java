package com.fedex.peripherals.installer.actions;

import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This Action class is used to perform the delete Installer action on any file or folder
 * 
 * @author Shagun Sharma [3696362]
 * @version 0.0.1
 * @since Oct 29, 2019
 */
public class PeripheralDeleteInstallerFiles extends PeripheralDeleteAction {

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Oct 29, 2019
     * @version 0.0.1
     */
    @Override
    protected void init() {
        sourcePath = (String) getProps().get(PeripheralSoftwareUpdateConstants.DELETION_DIR_PATH);
    }

}
