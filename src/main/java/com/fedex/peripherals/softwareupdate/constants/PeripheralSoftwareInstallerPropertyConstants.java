package com.fedex.peripherals.softwareupdate.constants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the final class containing constants to be used for installer config property file
 * 
 * @author Divyanshu Varshney [5209377]
 * @version 0.0.1
 * @since 18-Oct-2019
 */
public final class PeripheralSoftwareInstallerPropertyConstants {

    public static final String FILE_FOLDER_BACKUP_LIST = "peripheral.installer.fileToBeBackuped";
    public static final String EXECUTABLE_JAR_LOCATION = "peripheral.installer.ExecutableJarFileLocation";
    public static final String EMAIL_FROM = "peripheral.installer.email.emailFrom";
    public static final String PORT = "peripheral.installer.email.port";
    public static final String EMAIL_HOST = "peripheral.installer.email.emailHost";
    public static final String EMAIL_TO = "peripheral.installer.email.emailTo";
    public static final String EMAIL_SUBJECT_SUCCESS = "peripheral.installer.email.emailSubjectSuccess";
    public static final String EMAIL_SUBJECT_FAILURE = "peripheral.installer.email.emailSubjectFailure";
    public static final String EMAIL_SUBJECT_ACTION_REQUIRED = "peripheral.installer.email.emailSubjectActionRequired";
    public static final String MESSAGE_BODY_SUCCESS = "peripheral.installer.email.messageBodySuccess";
    public static final String MESSAGE_BODY_FAILURE = "peripheral.installer.email.messageBodyFailure";
    public static final String MESSAGE_BODY_ACTION_REQUIRED = "peripheral.installer.email.messageBodyActionRequired";
    public static final String ATTACHMENT_PATH = "peripheral.installer.email.attachmentPath";
    public static final String LOGO_PATH = "peripheral.installer.report.logoPath";
    public static final String REPORT_PATH = "peripheral.installer.report.pdf.path";
    public static final String INSTALLATION_LOCATION = "peripheral.installer.installationLocation";
    public static final String PERIPHERAL_ENABLED_DEVICES = "peripheral.enabledDevices";
    public static final String PERIPHERAL_SERVICE_PORT = "peripheral.server.port";
    public static final String PERIPHERAL_SERVER_ACTIVE_PROFILE = "peripheral.activeSpringProfile";

    private PeripheralSoftwareInstallerPropertyConstants() {
        throw new UnsupportedOperationException(PeripheralSoftwareInstallerPropertyConstants.class.getName());
    }



}
