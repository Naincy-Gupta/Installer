package com.fedex.peripherals.installer.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;
import com.fedex.peripherals.softwareupdate.utilities.PeripheralSoftwareUpdateUtilities;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the Action class used for verifying peripheral server installation
 * 
 * @author Divyanshu Varshney [5209377]
 * @version 0.0.1
 * @since 17-Oct-2019
 */
public class PeripheralInstallationVerificationAction extends PeripheralSoftwareUpdateAction {

    private String serverInformation;
    private String port;
    PeripheralSoftwareUpdateUtilities peripheralSoftwareUpdateUtilities;

    private static final Logger logger = LogManager.getLogger(PeripheralInstallationVerificationAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since Feb 13, 2020
     * @version 0.0.1
     */
    private void init() {
        logger.traceEntry("init method of PeripheralInstallationVerificationAction class");
        peripheralSoftwareUpdateUtilities = new PeripheralSoftwareUpdateUtilities();
        port = (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.PERIPHERAL_SERVICE_PORT);
        if (port.isEmpty()) {
            // setting the port to the default value that is 8080 when no port is provided in the properties files
            port = PeripheralSoftwareUpdateConstants.PERIPHERAL_SERVICE_DEFAULT_PORT;
        }
        logger.info("peripheral server service port: {}",port);
    }

    /**
     * This method execute the verification action for peripheral server installation
     * 
     * @return int
     * @since Oct 17, 2019
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralInstallationVerificationAction class");
        init();
        try {
            Thread.sleep(35000);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
            StringBuilder peripheralServerInfoEndpoint = new StringBuilder();
            peripheralServerInfoEndpoint.append("http://");
            peripheralServerInfoEndpoint.append(peripheralSoftwareUpdateUtilities.getPeripheralServerHostName());
            peripheralServerInfoEndpoint.append(":");
            peripheralServerInfoEndpoint.append(port);
            peripheralServerInfoEndpoint.append("/peripherals/fedexoffice/v1/serverinfo");
            ResponseEntity<Object> serverInfoResponse = restTemplate.exchange(peripheralServerInfoEndpoint.toString(),
                            HttpMethod.GET, requestEntity, Object.class);
            setServerInformation(serverInfoResponse.getBody().toString());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return logger.traceExit(PeripheralSoftwarePhasesErrorCode.VALIDATIONPHASE.getErrorCode());
        }
        return logger.traceExit(0);
    }


    public String getServerInformation() {
        return serverInformation;
    }


    public void setServerInformation(String serverInformation) {
        this.serverInformation = serverInformation;
    }


    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return 0;
    }

    @Override
    public boolean validateAction() {
        return true;
    }

    @Override
    public int cleanUp() {
        return 0;
    }

}
