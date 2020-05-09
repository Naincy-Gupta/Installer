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
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Implement application performance monitoring.<br/>
 * Description - This is the Action class used for starting the peripheral server
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class PeripheralServerStartAction extends PeripheralSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceStartActionStatus;
    private PeripheralSoftwarePhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(PeripheralServerStartAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        // update the location of winsw to installation folder
        winswExeLoc = (String) getProps().get(PeripheralSoftwareUpdateConstants.WINSW_EXE_LOC);
        serviceId = (String) getProps().get(PeripheralSoftwareUpdateConstants.SERVICE_ID);
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
    }

    /**
     * This method execute the Start Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralServerStartAction class");
        init();

        logger.debug("checking the state of service before starting peripheral server");
        String serviceState = getServiceState();
        if (serviceState != null && serviceState.contains("STOPPED")) {
            String installCommand = winswExeLoc + " start";
            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Starting peripheral server at {} ", getCurrentTime());
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
                    winServiceStartActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceStartActionStatus != 0) {
                    throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while starting peripheral server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        }
        return logger.traceExit(0);
    }

    /**
     * This method fetches the current state of the windows service
     * 
     * @return
     * @since 11-Oct-2019
     */
    public String getServiceState() {
        logger.traceEntry("getServiceState method of PeripheralServerStartAction class");
        PeripheralServerServiceStatusAction peripheralServerServiceStatusAction =
                        new PeripheralServerServiceStatusAction();
        Properties properties = new Properties();
        properties.setProperty("serviceId", serviceId);
        peripheralServerServiceStatusAction.setProps(properties);
        peripheralServerServiceStatusAction.setInstallationPhase(installationPhase);
        peripheralServerServiceStatusAction.execute();
        return logger.traceExit(peripheralServerServiceStatusAction.getServiceStatus());
    }

    /**
     * This method cleans up the intermediate file/folders created while performing Start action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method undo the Start Action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return 0;
    }

    /**
     * This method perform the validation of the Start action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralServerStartAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation != null && serviceStateValidation.contains("RUNNING")) {
            logger.info("Peripheral server has been started successsfully");
            return logger.traceExit(true);
        } else {
            logger.info("Could not start Peripheral server");
            return logger.traceExit(false);
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
