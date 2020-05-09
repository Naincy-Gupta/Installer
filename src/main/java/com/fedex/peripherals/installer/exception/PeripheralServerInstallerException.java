package com.fedex.peripherals.installer.exception;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the class is the PeripheralServerInstallerException class
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralServerInstallerException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int errorCode;

    public PeripheralServerInstallerException(int errorCode, String errorMessage,Throwable cause) {
        super(errorMessage,cause);
        this.errorCode = errorCode;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
