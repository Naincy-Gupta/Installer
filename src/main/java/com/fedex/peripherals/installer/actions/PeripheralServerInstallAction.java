package com.fedex.peripherals.installer.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;
import com.fedex.peripherals.softwareupdate.utilities.PeripheralSoftwareUpdateUtilities;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Implement application performance monitoring.<br/>
 * Description - This is the Action class used for installing peripheral server as a windows service
 * 
 * @author Shagun Sharma [3696362]
 * @version 1.0.0
 * @since 11-Oct-2019
 */
public class PeripheralServerInstallAction extends PeripheralSoftwareUpdateAction {

    private String winswExeLoc;
    private String serviceId;
    private int winServiceInstallActionStatus;
    private PeripheralSoftwareUpdateUtilities peripheralSoftwareUtility;
    private PeripheralSoftwarePhasesErrorCode installationPhase;

    private static final Logger logger = LogManager.getLogger(PeripheralServerInstallAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 11-Oct-2019
     */
    private void init() {
        //set winsw location to installer location 
        winswExeLoc =(String) getProps().get(PeripheralSoftwareUpdateConstants.WINSW_EXE_LOC);
        serviceId = (String) getProps().get(PeripheralSoftwareUpdateConstants.SERVICE_ID);
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        peripheralSoftwareUtility = new PeripheralSoftwareUpdateUtilities();
    }

    /**
     * This method execute the Install Action
     * 
     * @return int
     * @since 11-Oct-2019
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralServerInstallAction class");
        init();
        logger.debug("checking the state of service before installing peripheral server");
        if (getServiceState() == null) {
            String installCommand = winswExeLoc + " install";

            ProcessBuilder builder = new ProcessBuilder();
            logger.info("Installing peripheral server as windows service at {} ",
                            peripheralSoftwareUtility.getCurrentTime());
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
                    winServiceInstallActionStatus = p.waitFor();
                }
                Thread.sleep(15000);
                if (winServiceInstallActionStatus != 0) {
                    throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                    "An exception has occured while installing peripheral server", null);
                }
                p.destroy();
            } catch (Exception exception) {
                logger.error(ExceptionUtils.getStackTrace(exception));
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                                exception.getCause());
            }
        } else {
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            "Peripheral server is already installed on the system", null);
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
        logger.traceEntry("getServiceState method of PeripheralServerInstallAction class");
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
     * This method cleans up the intermediate file/folders created while performing Install action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method undo the Install action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        if (getServiceState() != null) {
            logger.info("Service exists proceeding to uninstall the service");
            PeripheralServerUninstallAction peripheralServerUninstallAction = new PeripheralServerUninstallAction();
            peripheralServerUninstallAction.setPeripheralSoftwareUpdatePhase(installationPhase);
            peripheralServerUninstallAction.setProps(getProps());
            peripheralServerUninstallAction.execute();
        } else {
            logger.debug("Service does not exist Uninstall not required");
        }

        return 0;
   }


    /**
     * This method perform the validation of the Install action
     * 
     * @return
     * @since 11-Oct-2019
     */
    @Override
    public boolean validateAction() {
        logger.traceEntry("validateAction method of PeripheralServerInstallAction class");
        String serviceStateValidation = getServiceState();
        if (serviceStateValidation != null && serviceStateValidation.contains("STOPPED")) {
            logger.info("Peripheral server has been successsfully installed as a windows service");
            return logger.traceExit(true);
        } else {
            logger.info("Could not install Peripheral server");
            return logger.traceExit(false);
        }
    }


}
