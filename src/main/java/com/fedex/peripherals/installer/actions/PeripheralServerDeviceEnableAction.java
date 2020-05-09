package com.fedex.peripherals.installer.actions;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

public class PeripheralServerDeviceEnableAction extends PeripheralSoftwareUpdateAction {

    private static final String PERIPHERAL_LIST_INDEXED_KEY = "peripheral.peripheralsList[{0}].{1}";
    private Properties props;
    private PeripheralPropertyFileUpdateAction peripheralPropertyFileUpdateAction;
    private PropertiesConfiguration peripheralServerPropertiesSnapshot;
    private static final Logger logger = LogManager.getLogger(PeripheralServerDeviceEnableAction.class);
    private String[] enabledDevicesArray;

    /**
     * This method initialize the parameters required by execute method
     * 
     * @return int
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    private void init() {
        logger.traceEntry("init method of PeripheralServerDeviceEnableAction class");
        props = getProps();
        peripheralPropertyFileUpdateAction = new PeripheralPropertyFileUpdateAction();
        String destination = (String) getProps().get(PeripheralSoftwareUpdateConstants.PROPERTY_FILE_LOCATION);
        Properties properties = new Properties();
        properties.setProperty(PeripheralSoftwareUpdateConstants.PROPERTY_FILE_LOCATION, destination);
        peripheralPropertyFileUpdateAction.setProps(properties);
        peripheralPropertyFileUpdateAction.setPropertyMap(Collections.emptyMap());
        peripheralServerPropertiesSnapshot = peripheralPropertyFileUpdateAction.getPropertiesSnapshot();
        String enabledDevices = props.getProperty(PeripheralSoftwareInstallerPropertyConstants.PERIPHERAL_ENABLED_DEVICES);
        if (enabledDevices != null && enabledDevices.length() > 0) {
            enabledDevicesArray = enabledDevices.split(",");
        }
    }

    /**
     * This method execute the Device Enable Action and updates enabled/disabled devices in the peripheral server
     * configuration properties file
     * 
     * @return int
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    @Override
    public int execute() {
        logger.traceEntry("execute method of PeripheralServerDeviceEnableAction class");
        init();
        final String PERIPHERAL_LOGICAL_NAME_KEY = "peripheralLogicalName";
        final String PERIPHERAL_TYPE_KEY = "peripheralType";
        String peripheralLogicalNameValue = "";
        String peripheralTypeValue = "";

        Map<String, String> propertyMap = new HashMap<>();
        boolean isListCorrect = false;
        if (enabledDevicesArray != null) {
            logger.debug("List of enabled device created");
            for (int i = 0; peripheralServerPropertiesSnapshot.containsKey(
                            MessageFormat.format(PERIPHERAL_LIST_INDEXED_KEY, i, PERIPHERAL_TYPE_KEY)); i++) {
                peripheralLogicalNameValue = peripheralServerPropertiesSnapshot.getString(
                                MessageFormat.format(PERIPHERAL_LIST_INDEXED_KEY, i, PERIPHERAL_LOGICAL_NAME_KEY));
                peripheralTypeValue = peripheralServerPropertiesSnapshot
                                .getString(MessageFormat.format(PERIPHERAL_LIST_INDEXED_KEY, i, PERIPHERAL_TYPE_KEY));
                if (!isDeviceEnabled(peripheralLogicalNameValue, peripheralTypeValue)) {
                    propertyMap.put(MessageFormat.format(PERIPHERAL_LIST_INDEXED_KEY, i, "disabled"),
                                    Boolean.toString(true));
                } else {
                    isListCorrect = true;
                }
            }
            if (isListCorrect) {
                peripheralPropertyFileUpdateAction.setPropertyMap(propertyMap);
                peripheralPropertyFileUpdateAction.execute();
                logger.debug("Disabled property of device updated in config file");
            }
        }
        return logger.traceExit(0);
    }

    /**
     * This method undo the changes done by execute method to peripheral server configuration properties file
     * 
     * @return int
     * @since Jan 31, 2020
     * @version 0.0.1
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return peripheralPropertyFileUpdateAction.undoPeripheralSoftwareUpdateAction();
    }

    /**
     * This method validate that peripheral server configuration properties are updated
     * 
     * @return boolean
     * @since Jan 31, 2020
     * @version 0.0.1
     */
    @Override
    public boolean validateAction() {
        return peripheralPropertyFileUpdateAction.validateAction();
    }

    /**
     * Unused
     * 
     * @return boolean
     * @since Jan 31, 2020
     * @version 0.0.1
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * Returns true if the device's type or logical name is in enabled device list
     * 
     * @return boolean
     * @since Feb 6, 2020
     * @version 0.0.1
     */
    private boolean isDeviceEnabled(String peripheralLogicalName, String peripheralType) {
        final String PERIPHERAL_TYPE_LABEL_PRINTER = "LABEL_PRINTER";
        final String PERIPHERAL_TYPE_LABEL_PRINTER_ZEBRA = "ZEBRA";
        if (peripheralType.equalsIgnoreCase(PERIPHERAL_TYPE_LABEL_PRINTER)) {
            for (int i = 0; i < enabledDevicesArray.length; i++) {
                if (enabledDevicesArray[i].equalsIgnoreCase(PERIPHERAL_TYPE_LABEL_PRINTER_ZEBRA)) {
                    if (peripheralLogicalName.toUpperCase().contains(enabledDevicesArray[i].toUpperCase())) {
                        return true;
                    }
                } else {
                    if (peripheralLogicalName.equalsIgnoreCase(enabledDevicesArray[i])) {
                        return true;
                    }
                }
            }
        } else {
            for (int i = 0; i < enabledDevicesArray.length; i++) {
                if (peripheralType.equalsIgnoreCase(enabledDevicesArray[i])) {
                    return true;
                }
            }
        }
        return false;
    }

}
