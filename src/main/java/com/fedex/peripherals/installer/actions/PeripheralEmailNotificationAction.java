package com.fedex.peripherals.installer.actions;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for sending email notification to provided group when peripheral server
 * installation is successful or failed
 * 
 * @author Divyanshu Varshney [5209377]
 * @version 0.0.1
 * @since 9-Oct-2019
 */
public class PeripheralEmailNotificationAction extends PeripheralSoftwareUpdateAction {

    private String emailFrom;
    private int emailPort;
    private String emailHost;
    private String emailTo;
    private String attachmentPaths;
    private String emailSubject;
    private String messageBody;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private boolean isInitialization;
    private int errorCode;
    private boolean actionRequired;
    private String serverInformation;
    private String temporaryMessageBody;
    private static final Logger logger = LogManager.getLogger(PeripheralEmailNotificationAction.class);

    /**
     * This method initialize the instance variable through by reading the value from XML file
     * 
     * @return void
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    private void init() {

        this.emailFrom = (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.EMAIL_FROM);
        this.emailPort = Integer.parseInt((String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.PORT));
        this.emailHost = (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.EMAIL_HOST);
        this.emailTo = (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.EMAIL_TO);
        this.attachmentPaths = (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.ATTACHMENT_PATH);
        installationPhase = this.getPeripheralSoftwareUpdatePhase();

    }
    
    /**
     * This method sets properties for email action and executes it.
     * 
     * @return int
     * @since Oct 11, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        org.apache.logging.log4j.message.Message messageLogger =
                        logger.traceEntry("execute method of PeripheralEmailNotificationAction class");

        if (serverInformation != null) {
            temporaryMessageBody = formatServerResponse(serverInformation);
        }
        // Conditionally setting up email subject and body depending upon peripheral server installation status
        if (actionRequired) {
            // Setting subject and body for action required mail
            this.emailSubject = (String) getProps()
                            .get(PeripheralSoftwareInstallerPropertyConstants.EMAIL_SUBJECT_ACTION_REQUIRED);
            this.messageBody = ((String) getProps()
                            .get(PeripheralSoftwareInstallerPropertyConstants.MESSAGE_BODY_ACTION_REQUIRED));
        } else if (errorCode > 0) {
            // Setting subject and body for failure mail
            this.emailSubject =
                            (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.EMAIL_SUBJECT_FAILURE);
            this.messageBody = ((String) getProps()
                            .get(PeripheralSoftwareInstallerPropertyConstants.MESSAGE_BODY_FAILURE));
        } else if (errorCode == 0 && !isInitialization) {
            // Setting subject and body for success mail
            this.emailSubject =
                            (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.EMAIL_SUBJECT_SUCCESS);
            this.messageBody =
                            ((String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.MESSAGE_BODY_SUCCESS)
                                            + temporaryMessageBody);
        }

        // added error code level 2 and 3 for Initialization
        if ((isInitialization && errorCode != 2 && errorCode != 3) || !isInitialization) {
            //Triggering the email action
            logger.debug("Calling  the execute method of email action");
            triggerEmailAction();
        }
        return logger.traceExit(messageLogger, 0);
    }

    /**
     * This method formats server response set by PeripheralInstallationVerificationAction
     * 
     * @return String
     * @since Oct 17, 2019
     * @version 0.0.1
     */
    private String formatServerResponse(String serverInformation) {
        logger.traceEntry("formatServerResponse method of PeripheralSoftwareUpdateProject class");
        String[] temporaryTokens;
        String[] parts;
        StringBuilder formattedData = new StringBuilder();

        String temperaryFormattedResponse = serverInformation.substring(serverInformation.lastIndexOf('{') + 1);
        String formattedResponse = temperaryFormattedResponse.substring(temperaryFormattedResponse.lastIndexOf('{') + 1)
                        .substring(0, temperaryFormattedResponse.indexOf('}'));
        parts = formattedResponse.split(",");
        formattedData.append(
                        "<br><br><table style='border:1px solid; border-collapse:collapse'><tr><th style='border:1px solid'>IP Address</th><th style='border:1px solid'>Host Name</th><th style='border:1px solid'>Build Version</th></tr><tr>");
        for (String token : parts) {
            temporaryTokens = token.split("=");
            formattedData.append("<td style='border:1px solid'>" + temporaryTokens[1] + "</td>");

        }
        formattedData.append("</tr></table> <br><br>Thanks");
        return logger.traceExit(formattedData.toString());
    }

    /**
     * This method execute the email notification action
     * 
     * @return int
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    
    public int triggerEmailAction() {
        org.apache.logging.log4j.message.Message messageLogger = logger.traceEntry("triggerEmailAction method of PeripheralEmailNotificationAction class");
        init();
        
        // Making Properties object which will further pass as argument to Session object
        Properties props = new Properties();
        props.put("mail.smtp.host", this.emailHost);
        props.put("mail.smtp.port", Integer.valueOf(this.emailPort));
        
        //Setting up a mail session
        Session session = Session.getInstance(props);

        try {
        	InternetAddress[] receiversAddress = InternetAddress.parse(this.emailTo, true);
        	
            InetAddress addr = InetAddress.getLocalHost();
            String machineName = addr.getHostName();
            
            // Setting up message properties
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.emailFrom));
            message.setRecipients(Message.RecipientType.TO, receiversAddress);
            Multipart multipart = new MimeMultipart();

            List<String> fileNames = getAttachmentsName(this.attachmentPaths);
            for (String fName : fileNames) {
                File file = new File(fName);
                if (file.exists()) {
                    addAttachment(multipart, fName);
                }
            }
            BodyPart htmlBodyPart = new MimeBodyPart(); 
            htmlBodyPart.setContent(this.messageBody , "text/html");
            multipart.addBodyPart(htmlBodyPart);
            message.setContent(multipart);
            this.emailSubject = System.getenv("ctrNumber") != null
                            ? this.emailSubject + machineName + "-" + System.getenv("ctrNumber")
                            : this.emailSubject + machineName;
            message.setSubject(this.emailSubject);
            logger.debug("Sending email notification");
            Transport.send(message);
        } catch (MessagingException messagingException) {
            logger.error(messagingException.getMessage());
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),messagingException.getMessage(),
            		messagingException.getCause());
        } catch (UnknownHostException unknownHostException) {
            logger.error(unknownHostException.getMessage());
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),unknownHostException.getMessage(),
            		unknownHostException.getCause());
        }

        return logger.traceExit(messageLogger, 0);
    }

    /**
     * This method adds attachment in the email to be sent for installation process
     * 
     * @param multipart
     * @param fileName
     * @return void
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    private static void addAttachment(Multipart multipart, String fileName) throws MessagingException {
        logger.traceEntry("addAttachment method of PeripheralEmailNotificationAction class");
        DataSource source = new FileDataSource(fileName);
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(fileName.substring(fileName.lastIndexOf('/')));
        logger.info("Adding attachements in email");
        multipart.addBodyPart(messageBodyPart);
        logger.trace("Exit addAttachment()");
    }

    /**
     * This method gets filenames for attachment to be added in email for installation process
     * 
     * @param attachementsName
     * @return List<String>
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    private List<String> getAttachmentsName(String attachementsName) {
        logger.traceEntry("getAttachmentsName method of PeripheralEmailNotificationAction class");
        logger.info("Fetching filename of attachments");
        List<String> attachments = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(attachementsName, "|");
        while (tokenizer.hasMoreTokens()) {
            attachments.add(tokenizer.nextToken());
        }
        return logger.traceExit( attachments);
    }

    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return 0;
    }

    @Override
    public boolean validateAction() {
        return false;
    }

    @Override
    public int cleanUp() {
        return 0;
    }

	public boolean isInitialization() {
		return isInitialization;
	}

	public void setInitialization(boolean isInitialization) {
		this.isInitialization = isInitialization;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public boolean isActionRequired() {
		return actionRequired;
	}

	public void setActionRequired(boolean actionRequired) {
		this.actionRequired = actionRequired;
	}

	public String getServerInformation() {
		return serverInformation;
	}

	public void setServerInformation(String serverInformation) {
		this.serverInformation = serverInformation;
	}

}
