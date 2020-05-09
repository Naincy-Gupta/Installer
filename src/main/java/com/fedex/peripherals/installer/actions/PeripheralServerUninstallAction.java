package com.fedex.peripherals.installer.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br>
 * 
 * Theme - Core Retail Peripheral Services<br>
 * Feature - Peripheral Services - Implement application performance
 * monitoring.<br>
 * Description - This is the Action class used for uninstalling the peripheral
 * server
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class PeripheralServerUninstallAction extends PeripheralSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceUninstallActionStatus;
    private PeripheralSoftwarePhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(PeripheralServerUninstallAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML
     * file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        winswExeLoc = (String) getProps().get(PeripheralSoftwareUpdateConstants.WINSW_EXE_LOC);
        serviceId = (String) getProps().get(PeripheralSoftwareUpdateConstants.SERVICE_ID);
        installationPhase=this.getPeripheralSoftwareUpdatePhase();
    }

    /**
     * This method execute the Uninstall Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralServerUninstallAction class");
        init();

        logger.debug("checking the state of service before uninstalling peripheral server");
        String serviceState = getServiceState();
        if (serviceState == null) {
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), "Service doesnt exist",
                            null);
        } else if (serviceState.contains("RUNNING")) {
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Peripheral server is in running state, please stop the service and try again", null);
        } else if (serviceState.contains("STOPPED")) {
            String installCommand = winswExeLoc + " uninstall";
            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Uninstalling peripheral server at {}", getCurrentTime());
            builder.command("cmd.exe", "/c", installCommand);

            builder.redirectErrorStream(true);
            Process p;
            try {
                p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    logger.info(line);
                    winServiceUninstallActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceUninstallActionStatus != 0) {
                    throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while uninstalling peripheral server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        }
        return logger.traceExit( 0);
    }
    
    /**
     * This method fetches the current state of the windows service
     * 
     * @return
     * @since 11-Oct-2019
     */
    public String getServiceState() {
        logger.traceEntry("getServiceState method of PeripheralServerUninstallAction class");
        PeripheralServerServiceStatusAction peripheralServerServiceStatusAction = new PeripheralServerServiceStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        peripheralServerServiceStatusAction.setProps(properties);
        peripheralServerServiceStatusAction.setInstallationPhase(installationPhase);
        peripheralServerServiceStatusAction.execute();
        return logger.traceExit( peripheralServerServiceStatusAction.getServiceStatus());
    }

    /**
     * This method undo the Uninstall Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method cleans up the intermediate file/folders created while performing
     * Uninstall action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method perform the validation of the Uninstall action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralServerUninstallAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation == null) {
            logger.info("Peripheral server has been successsfully uninstalled");
            return logger.traceExit( true);
        } else {
            logger.info("Could not uninstall Peripheral server");
            return logger.traceExit( false);
        }
    }
    
    /**
     * This method gets the current local date and time
     * 
     * @return current local date and time in String format
     * @since 11-Oct-2019
     */
    private String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }
}
