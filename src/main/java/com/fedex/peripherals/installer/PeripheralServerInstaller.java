package com.fedex.peripherals.installer;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.actions.PeripheralEmailNotificationAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;
import com.fedex.peripherals.softwareupdate.foundation.PeripheralProject;
import com.fedex.peripherals.softwareupdate.foundation.PeripheralProperty;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the main class used for reading the XML file and for the startup task of peripheral server
 * installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralServerInstaller {

    private String xmlPath;
    private String configPropertyPath;
    private String installationPath;
    private PeripheralSoftwareUpdateProject peripheralSoftwareUpdateProject;
    private static final Logger logger = LogManager.getLogger(PeripheralServerInstaller.class);

    /**
     * @param xmlPath
     * @param argConfigPropertyPath
     * @param argnstallationPath
     */
    public PeripheralServerInstaller(String xmlPath, String argConfigPropertyPath, String argnstallationPath) {
        this.xmlPath = xmlPath;
        this.configPropertyPath = argConfigPropertyPath;
        this.installationPath = argnstallationPath;
    }

    /**
     * @param xmlPath
     * @param argConfigPropertyPath
     */
    public PeripheralServerInstaller(String xmlPath, String argConfigPropertyPath) {
        this.xmlPath = xmlPath;
        this.configPropertyPath = argConfigPropertyPath;
    }

    /**
     * This method read the Xml file present at a predefined location
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param configPropertyPath2
     */
    private void loadInstallationProject() {
        logger.traceEntry("loadInstallationProject method of PeripheralServerInstaller class");
        PeripheralProject peripheralProject = null;
        File fileXml = new File(xmlPath);
        try {
            logger.debug("Reading XML file and Unmarshalling it");
            if (fileXml.exists()) {
                JAXBContext context = JAXBContext.newInstance(PeripheralProject.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                peripheralProject = (PeripheralProject) unmarshaller.unmarshal(fileXml);
                peripheralSoftwareUpdateProject = new PeripheralSoftwareUpdateProject(peripheralProject,
                                configPropertyPath, installationPath);
            } else {
                logger.error("Installer script is not present at provided location, {}",
                                PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorMessage());
                triggerFailureMail(this.configPropertyPath);
            }
        } catch (JAXBException jaxbException) {
            logger.error("Exception occured while parsing {}", fileXml);
            throw new PeripheralServerInstallerException(
                            PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorCode(),
                            jaxbException.getMessage(), jaxbException.getCause());
        }
        logger.trace("Exit loadInstallationProject()");
    }

    /**
     * This method called by the main method having all the startup logic
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     * @param configPropertyPath
     */
    public void startPeripheralUpdate() {
        logger.traceEntry("startPeripheralUpdate method of PeripheralServerInstaller class");
        try {
            loadInstallationProject();
            if (peripheralSoftwareUpdateProject != null) {
                logger.debug("calls the execute method of peripheralSoftwareUpdateProject when peripheralSoftwareUpdateProject object is not null ");
                peripheralSoftwareUpdateProject.execute();
            } else {
                logger.warn("No actions defined");
            }
        } catch (PeripheralServerInstallerException peripheralServerInstallerException) {
            logger.error(ExceptionUtils.getStackTrace(peripheralServerInstallerException));
            throw new PeripheralServerInstallerException(
                            PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorCode(),
                            peripheralServerInstallerException.getMessage(),
                            peripheralServerInstallerException.getCause());
        }

        logger.trace("Exit startPeripheralUpdate()");
    }

    /**
     * This method triggers failure in case o errorneous scenario
     * 
     * @return void
     * @since Oct 18, 2019
     * @version 0.0.1
     * @param configPath
     */
    private static void triggerFailureMail(String configPath) {
        logger.traceEntry("triggerFailureMail method of PeripheralServerInstaller class");
        PeripheralEmailNotificationAction peripheralEmailNotificationAction = new PeripheralEmailNotificationAction();
        PeripheralSoftwareLoadProperties peripheralSoftwareLoadProperties =
                        new PeripheralSoftwareLoadProperties(configPath);
        PeripheralSoftwareActionLoader propertyResolver = new PeripheralSoftwareActionLoader();
        
        List<PeripheralProperty> property = new ArrayList<>();
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.EMAIL_FROM,
                        peripheralSoftwareLoadProperties.getProperty()
                                        .getProperty(PeripheralSoftwareInstallerPropertyConstants.EMAIL_FROM)));
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.PORT, peripheralSoftwareLoadProperties
                        .getProperty().getProperty(PeripheralSoftwareInstallerPropertyConstants.PORT)));
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.EMAIL_HOST,
                        peripheralSoftwareLoadProperties.getProperty()
                                        .getProperty(PeripheralSoftwareInstallerPropertyConstants.EMAIL_HOST)));
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.EMAIL_TO, peripheralSoftwareLoadProperties
                        .getProperty().getProperty(PeripheralSoftwareInstallerPropertyConstants.EMAIL_TO)));
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.ATTACHMENT_PATH,
                        peripheralSoftwareLoadProperties.getProperty()
                                        .getProperty(PeripheralSoftwareInstallerPropertyConstants.ATTACHMENT_PATH)));
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.EMAIL_SUBJECT, peripheralSoftwareLoadProperties
                        .getProperty().getProperty(PeripheralSoftwareInstallerPropertyConstants.EMAIL_SUBJECT_FAILURE)));
        property.add(new PeripheralProperty(PeripheralSoftwareUpdateConstants.MESSAGE_BODY, peripheralSoftwareLoadProperties
                        .getProperty().getProperty(PeripheralSoftwareInstallerPropertyConstants.MESSAGE_BODY_FAILURE)));
        
        Properties properties = propertyResolver.buildPropertiesForAction(property,
                        peripheralSoftwareLoadProperties.getProperty());
        peripheralEmailNotificationAction.setProps(properties);
        logger.debug("Calling  the execute method of email action");
        peripheralEmailNotificationAction.execute();
        System.exit(PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorCode());
        logger.trace("Exit triggerFailureMail()");
    }

    /**
     * This method validates the Input arguments and returns the result
     * 
     * @param args
     * @return Arguments are correct or not
     */
    private static boolean isInputParametersValidationFailed(String[] args) {
        boolean isInputParameterValidationFailed = false;
        if (args.length < 2 || ((args[0].isEmpty() && args[0] == null) && (args[1].isEmpty() && args[1] == null))) {
            isInputParameterValidationFailed = true;
        }
        return isInputParameterValidationFailed;
    }

    public static void main(String... args) {
        try {
            if (isInputParametersValidationFailed(args)) {
                logger.error("Invalid Arguments, {}",
                                PeripheralSoftwarePhasesErrorCode.PERIPHERAL_INSTALLER_FAILURE.getErrorMessage());
                triggerFailureMail(args[1]);
            } else {
                PeripheralServerInstaller peripheralServerInstaller =
                                args.length == 3 ? new PeripheralServerInstaller(args[0], args[1], args[2])
                                                : new PeripheralServerInstaller(args[0], args[1]);
                peripheralServerInstaller.startPeripheralUpdate();
            }
        } catch (PeripheralServerInstallerException peripheralServerInstallerException) {
            logger.error(ExceptionUtils.getStackTrace(peripheralServerInstallerException));
            if (!((peripheralServerInstallerException.getCause() instanceof SocketException)
                            || (peripheralServerInstallerException.getCause() instanceof MessagingException)
                            || (peripheralServerInstallerException.getCause() instanceof UnknownHostException)
                            || (peripheralServerInstallerException.getCause() instanceof ConnectException))) {
                triggerFailureMail(args[1]);
            }
        }
    }
}
