package com.fedex.peripherals.softwareupdate.utilities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This is the class used for creating the class through reflection in peripheral server installer also it
 * will have utility method
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralSoftwareUpdateUtilities {
    private static final Logger logger = LogManager.getLogger(PeripheralSoftwareUpdateUtilities.class);

    /**
     * This method create the class using reflection
     * 
     * @param fullClassName
     * @return Object
     * @since Oct 4, 2019
     * @version 0.0.1
     * @throws UnknownHostException 
     */
    public Object createClass(String fullClassName)
                    throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        logger.traceEntry(
                        "createClass method of PeripheralSoftwareUpdateUtilities class that is creating the object of {}",
                        fullClassName);
        try {
            logger.traceExit();
            return Class.forName(fullClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new InstantiationException(
                            fullClassName + PeripheralSoftwareUpdateConstants.EOL + exception.getMessage());
        }

    }

    /**
     * This method gets the current local date and time
     * 
     * @return current local date and time in String format
     * @since 11-Oct-2019
     */
    public String getCurrentTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        return dtf.format(LocalDateTime.now());
    }

    /**
     * This method returns local host properties
     * 
     * @return InetAddress
     * @since 20-Sep-2019
     * @version 1.0.0
     */
    public InetAddress getLocalHost() throws UnknownHostException {
        logger.traceEntry("getLocalHost method of PeripheralUpdateHostNameAction class");
        InetAddress localHost;
        logger.debug("Reteriving the ip address using the getLocalHost method of InetAddress");
        localHost = InetAddress.getLocalHost();
        return logger.traceExit(localHost);
    }

    /**
     * This method returns the hostName of peripheral server
     * 
     * @return String
     * @since 20-Sep-2019
     * @version 1.0.0
     * @throws UnknownHostException 
     */
    public String getPeripheralServerHostName() throws UnknownHostException {
        logger.debug("Reteriving the host name using the getCanonicalHostName method of InetAddress");
        return getLocalHost().getCanonicalHostName();
    }
}
