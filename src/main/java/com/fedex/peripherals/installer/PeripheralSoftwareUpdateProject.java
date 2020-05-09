package com.fedex.peripherals.installer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.actions.PeripheralEmailNotificationAction;
import com.fedex.peripherals.installer.actions.PeripheralInstallationInitializationAction;
import com.fedex.peripherals.installer.actions.PeripheralPDFGeneration;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;
import com.fedex.peripherals.softwareupdate.foundation.ActionStatusInfo;
import com.fedex.peripherals.softwareupdate.foundation.PeripheralProject;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for executing the actions peripheral server installer
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralSoftwareUpdateProject {

    private String projectName;
    private String projectDescription;
    private String installationPath;
    private String configPropertyPath;
    private List<PeripheralSoftwareUpdateAction> peripheralSoftwareUpdateActions = new ArrayList<>();
    private PeripheralSoftwareUpdate peripheralSoftwareUpdate;
    private PeripheralSoftwareActionLoader peripheralSoftwareActionLoader;
    private PeripheralSoftwareLoadProperties peripheralSoftwareLoadProperties;
    private PeripheralPDFGeneration peripheralPDFGeneration;
    private static final Logger logger = LogManager.getLogger(PeripheralSoftwareUpdateProject.class);

    public PeripheralSoftwareUpdateProject(final PeripheralProject project, String configPropertyPath,
                    String argInstallationPath) {
        peripheralSoftwareUpdate = new PeripheralSoftwareUpdate();
        peripheralSoftwareActionLoader = new PeripheralSoftwareActionLoader();
        peripheralSoftwareLoadProperties = new PeripheralSoftwareLoadProperties(configPropertyPath);
        init(project, argInstallationPath);
        this.configPropertyPath = configPropertyPath;
    }

    public void init(PeripheralProject project, String argInstallationPath) {
        setProjectName(project.getName());
        setProjectDescription(project.getDescription());
        setPeripheralSoftwareUpdateActions(peripheralSoftwareActionLoader.buildActions(project.getActions(),
                        peripheralSoftwareLoadProperties, argInstallationPath));
        setInstallationPath(argInstallationPath);
    }

    /**
     * This method execute the action that need to be performed
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public void execute() {
        logger.traceEntry("execute method of PeripheralSoftwareUpdateProject class");
        int errorCode;
        boolean isInitialization = false;

        // variables for generating report
        String startTime = getCurrentTime();
        String previousPhase = null;
        Map<String, List<ActionStatusInfo>> reportTables = new LinkedHashMap<>();
        String currentPhase = "";
        PeripheralEmailNotificationAction peripheralEmailNotificationAction = null;

        Iterator<PeripheralSoftwareUpdateAction> iterator = peripheralSoftwareUpdateActions.iterator();
        while (iterator.hasNext()) {
            PeripheralSoftwareUpdateAction actionToBePerformed = iterator.next();
            if (actionToBePerformed.isActive() && peripheralSoftwareUpdate.isPerformFurtherActions()
                            && !(actionToBePerformed instanceof PeripheralEmailNotificationAction)) {
                logger.debug("Calls the Execute method of action manger for each action defined in the xml");
                if (actionToBePerformed instanceof PeripheralInstallationInitializationAction) {
                    ((PeripheralInstallationInitializationAction) actionToBePerformed)
                                    .setConfigFileLocation(configPropertyPath);
                    isInitialization = true;
                }
                boolean actionStatus = peripheralSoftwareUpdate.executeAction(actionToBePerformed);
                logger.info("action status" + actionToBePerformed.getName() + " status code "
                                + peripheralSoftwareUpdate.getStatusCode());
                if (!isInitialization) {
                    currentPhase = actionToBePerformed.getPeripheralSoftwareUpdatePhase().name();
                    ActionStatusInfo pdfReportDTO = new ActionStatusInfo(actionToBePerformed.getName(),
                                    actionToBePerformed.getDescription(), actionStatus);
                    if (currentPhase.equals(previousPhase)) {
                        reportTables.get(currentPhase).add(pdfReportDTO);
                    } else {
                        List<ActionStatusInfo> actionsList = new ArrayList<>();
                        actionsList.add(pdfReportDTO);
                        reportTables.put(currentPhase, actionsList);
                        previousPhase = currentPhase;
                    }
                }
            } else if (actionToBePerformed.isActive() && actionToBePerformed instanceof PeripheralEmailNotificationAction) {
                peripheralEmailNotificationAction = (PeripheralEmailNotificationAction) actionToBePerformed;
                peripheralEmailNotificationAction.setActionRequired(peripheralSoftwareUpdate.isActionRequired());
                peripheralEmailNotificationAction.setErrorCode(peripheralSoftwareUpdate.getStatusCode());
                peripheralEmailNotificationAction.setInitialization(isInitialization);
                peripheralEmailNotificationAction.setInstallationPath(getInstallationPath());
                peripheralEmailNotificationAction.setServerInformation(peripheralSoftwareUpdate.getServerInformation());
            }
        }
        errorCode = peripheralSoftwareUpdate.getStatusCode();
        String finalStatus = getFinalStatus(peripheralSoftwareUpdate.isActionRequired(), errorCode, isInitialization);
        List<ActionStatusInfo> cleanUpActions = peripheralSoftwareUpdate.cleanUp();
        if (!cleanUpActions.isEmpty()) {
            reportTables.put(PeripheralSoftwarePhasesErrorCode.CLEANUP.name(), cleanUpActions);
        }
        peripheralPDFGeneration = new PeripheralPDFGeneration();
        if (!isInitialization) {
            generatePDFReport(reportTables, finalStatus, startTime);
        }
		if (peripheralEmailNotificationAction != null) {
			peripheralEmailNotificationAction.execute();
		}
        if (!isInitialization) {
            peripheralPDFGeneration.cleanUp();
        }
        logger.trace("Exit execute()");
        System.exit(errorCode);
    }
   
    private void generatePDFReport(Map<String, List<ActionStatusInfo>> reportTables, String finalStatus,
                    String startTime) {
        Properties properties = new Properties();
        properties.setProperty(PeripheralSoftwareUpdateConstants.LOGO_PATH, peripheralSoftwareLoadProperties
                        .getProperty().getProperty(PeripheralSoftwareInstallerPropertyConstants.LOGO_PATH));
        properties.setProperty(PeripheralSoftwareUpdateConstants.REPORT_PATH, peripheralSoftwareLoadProperties
                        .getProperty().getProperty(PeripheralSoftwareInstallerPropertyConstants.REPORT_PATH));
        peripheralPDFGeneration.setInstallationPath(getInstallationPath());
        peripheralPDFGeneration.setProps(properties);
        peripheralPDFGeneration.setfinalStatus(finalStatus);
        peripheralPDFGeneration.setStartTime(startTime);
        peripheralPDFGeneration.setEndTime(getCurrentTime());
        peripheralPDFGeneration.setReportTables(reportTables);
        peripheralPDFGeneration.execute();
    }

    private String getFinalStatus(boolean isActionRequired, int errorCode, boolean isInitialization) {
        String finalStatus = PeripheralSoftwareUpdateConstants.FAILED_STATUS;
        if (isActionRequired) {
            finalStatus = PeripheralSoftwareUpdateConstants.ACTION_REQUIRED_STATUS;
        } else if (isInitialization && (errorCode == 2 || errorCode == 3)) {
            // for initialization step if the error code is 2 or 3 mark the final status as success
            finalStatus = PeripheralSoftwareUpdateConstants.SUCCESS_STATUS;
        } else if (errorCode == 0) {
            finalStatus = PeripheralSoftwareUpdateConstants.SUCCESS_STATUS;
        }
        return finalStatus;
    }

    private String getCurrentTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm:ss");
        LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.format(formatter);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public List<PeripheralSoftwareUpdateAction> getPeripheralSoftwareUpdateActions() {
        return peripheralSoftwareUpdateActions;
    }

    public void setPeripheralSoftwareUpdateActions(
                    List<PeripheralSoftwareUpdateAction> peripheralSoftwareUpdateActions) {
        this.peripheralSoftwareUpdateActions = peripheralSoftwareUpdateActions;
    }

    public String getInstallationPath() {
        return installationPath;
    }

    public void setInstallationPath(String installationPath) {
        this.installationPath = installationPath;
    }

}
