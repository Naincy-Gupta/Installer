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
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br><br>
 * 
 * Theme - Core Retail Peripheral Services<br>
 * Feature - Peripheral Services - Implement application performance
 * monitoring.<br>
 * Description - This is the Action class used for stopping the peripheral
 * server
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class PeripheralServerStopAction extends PeripheralSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceStopActionStatus;
    private PeripheralSoftwarePhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(PeripheralServerStopAction.class);

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
     * This method execute the Stop Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralServerStopAction class");
        init();

        logger.debug("checking the state of service before stopping peripheral server");
        String serviceState = getServiceState();
        if (serviceState != null && serviceState.contains("RUNNING")) {
            String installCommand = winswExeLoc + " stop";
            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Stopping peripheral server at {}", getCurrentTime());
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
                    winServiceStopActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceStopActionStatus != 0) {
                    throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while stopping peripheral server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        } else {
            logger.error("Peripheral server is already in stopped state");
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
        logger.traceEntry("getServiceState method of PeripheralServerStopAction class");
        PeripheralServerServiceStatusAction peripheralServerServiceStatusAction = new PeripheralServerServiceStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        peripheralServerServiceStatusAction.setProps(properties);
        peripheralServerServiceStatusAction.setInstallationPhase(installationPhase);
        peripheralServerServiceStatusAction.execute();
        return logger.traceExit( peripheralServerServiceStatusAction.getServiceStatus());
    }
    
    /**
     * This method undo the Stop Action
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
     * Stop action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method perform the validation of the Stop action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralServerStopAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation!= null && serviceStateValidation.contains("STOPPED")) {
            logger.info("Peripheral server has been stopped successsfully");
            return logger.traceExit( true);
        } else {
            logger.info("Could not stop Peripheral server");
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
