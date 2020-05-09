package com.fedex.peripherals.softwareupdate.constants;
/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the enum class conatins the Constant for the different phases with there error codes
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public enum PeripheralSoftwarePhasesErrorCode {

	COPYDEPENDENCYPHASE(101, "Exception occured while copying the dependency"),
	INSTALLATIONPHASE(201, "Exception occured while installation process"),
    APPLICATION_RUN_PHASE(301,"Exception occured while starting"),
    REPORTINGPHASE(401,"Exception occured while sending mail"),
    PERIPHERAL_INSTALLER_FAILURE(500,"Peripheral Installer Failure Occured"),
    CLEANUP(601,"Exception occured while cleanup"),
    VALIDATIONPHASE(701,"Exception occured while validating the process"),
    BUIDIDENTIFICATIONPHASE(801, "Exception occured while identifying build to install (incremental or full)"),
    APPLICATION_STOP_PHASE(901, "Exception occured while stoping");
	
    private int errorCode;
    private String errorMessage;

    PeripheralSoftwarePhasesErrorCode(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
