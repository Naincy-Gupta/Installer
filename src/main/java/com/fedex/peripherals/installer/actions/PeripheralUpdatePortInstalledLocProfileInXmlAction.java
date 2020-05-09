package com.fedex.peripherals.installer.actions;

import java.io.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareInstallerPropertyConstants;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature -PI 20.5 OTP Device Virtualization Implementation and Packaging<br/>
 * Description - This is the Action class used for updating the port , profile, and installed location from properties
 * file to xml
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 16-Apr-2020
 */
public class PeripheralUpdatePortInstalledLocProfileInXmlAction extends PeripheralSoftwareUpdateAction {
    private String peripheralServerPort;
    private String peripheralServerInstalledLoc;
    private String peripheralServerActiveProfile;
    private String peripheralServerXmlPath;
    private List<String> envValuesToBeUpdated;
    private String oldXMlPort;
    private String oldXMlProfile;
    private String oldXMlInstalledLoc;
    private boolean isUndo;
    private Document doc;
    private File file;
    private int noOfVarGetUpdated;
    private int noOfValuesToBeValidated;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private int updateCounter = 0;
    private String initialTag = "service";
    private DocumentBuilderFactory docFactory;
    private boolean initialException = false;
    public static final String PERIPHERAL_SERVICE_PORT = "PERIPHERAL-SERVER-PORT";
    public static final String PERIPHERAL_SERVICE_ACTIVE_PROFILE = "PERIPHERAL-SERVER-ACTIVE-PROFILE";
    public static final String INSTALLATION_LOCATION = "PERIPHERAL-SERVER-INSTALLED-LOCATION";
    private static final Logger logger = LogManager.getLogger(PeripheralUpdatePortInstalledLocProfileInXmlAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 16-Apr-2020
     */
    public void init() {
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        peripheralServerPort =
                        (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.PERIPHERAL_SERVICE_PORT);
        peripheralServerInstalledLoc =
                        (String) getProps().get(PeripheralSoftwareInstallerPropertyConstants.INSTALLATION_LOCATION);
        peripheralServerActiveProfile = (String) getProps()
                        .get(PeripheralSoftwareInstallerPropertyConstants.PERIPHERAL_SERVER_ACTIVE_PROFILE);
        peripheralServerXmlPath =
                        (String) getProps().get(PeripheralSoftwareUpdateConstants.PERIPHERAL_SERVER_INSTALLER_PATH);
        if (peripheralServerPort.isEmpty()) {
            // setting the port to the default value that is 8080 when no port is provided in the properties files
            peripheralServerPort = PeripheralSoftwareUpdateConstants.PERIPHERAL_SERVICE_DEFAULT_PORT;
        }
        envValuesToBeUpdated = new ArrayList<>();
        envValuesToBeUpdated.add(PERIPHERAL_SERVICE_ACTIVE_PROFILE);
        envValuesToBeUpdated.add(PERIPHERAL_SERVICE_PORT);
        envValuesToBeUpdated.add(INSTALLATION_LOCATION);
        noOfVarGetUpdated = 0;
        noOfValuesToBeValidated = 0;
        try {
            docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException parserConfigurationException) {
            initialException = true;
            logger.error(ExceptionUtils.getStackTrace(parserConfigurationException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            parserConfigurationException.getMessage(), parserConfigurationException.getCause());
        }
    }

    /**
     * This method execute the PeripheralUpdatePortInstalledLocProfileInXml Action
     * 
     * @return
     * @since 16-Apr-2020
     */
    @Override
    public int execute() {
        init();
        updateXml();
        return 0;
    }

    /**
     * @throws TransformerFactoryConfigurationError
     */
    private void updateXml() throws TransformerFactoryConfigurationError {
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            file = new File(peripheralServerXmlPath);
            if (file.exists()) {
                doc = docBuilder.parse(file);
                // Get the root element
                Node serviceNode = doc.getElementsByTagName(initialTag).item(0);
                NodeList serviceChildNodes = serviceNode.getChildNodes();
                updateXMLValues(envValuesToBeUpdated, serviceChildNodes);
                // write the content into xml file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            } else {
                if (!isUndo) {
                    initialException = true;
                }
                logger.error("File {} not found at the specified location", file);
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                "File not exists at the specified location" + " " + file, null);
            }
        } catch (TransformerException | SAXException | IOException | ParserConfigurationException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                            exception.getCause());
        }
    }

    /**
     * This method update the peripheral-server.xml env values like port, profile, installed location
     * 
     * @return
     * @since 16-Apr-2020
     */
    private void updateXMLValues(List<String> list, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node element = nodes.item(i);
            if ("env".equals(element.getNodeName())) {
                NamedNodeMap attributes = element.getAttributes();
                Node item = attributes.getNamedItem("name");
                for (String valueToBeUpdated : list) {
                    setValue(attributes, item, valueToBeUpdated);
                }
            }
        }
        if (isUndo) {
            updateCounter = 0;
            isUndo = false;
        } else {
            updateCounter++;
        }
    }

    /**
     * This method set the env values to its updated value
     * 
     * @return
     * @since 16-Apr-2020
     */
    private void setValue(NamedNodeMap attributes, Node item, String valueToBeUpdated) {
        if (item.getTextContent().equals(valueToBeUpdated)) {
            Node valueNode = attributes.getNamedItem("value");
            if (!isUndo) {
                setValueToXML(item, valueNode);
            } else {
                setValueToXMLInUndoCase(item, valueNode);
            }
        }
    }

    /**
     * This method set the env values like port, profile, installed location to its updated value in case of undo.
     * 
     * @return
     * @since 16-Apr-2020
     */
    private void setValueToXMLInUndoCase(Node item, Node valueNode) {
        if (PERIPHERAL_SERVICE_PORT.equals(item.getTextContent())) {
            valueNode.setTextContent(oldXMlPort);
        }
        if (PERIPHERAL_SERVICE_ACTIVE_PROFILE.equals(item.getTextContent())) {
            valueNode.setTextContent(oldXMlProfile);
        }
        if (INSTALLATION_LOCATION.equals(item.getTextContent())) {
            valueNode.setTextContent(oldXMlInstalledLoc);
        }
    }

    /**
     * This method set the env values like port, profile, installed location to its updated value.
     * 
     * @return
     * @since 16-Apr-2020
     */
    private void setValueToXML(Node item, Node valueNode) {
        if (PERIPHERAL_SERVICE_PORT.equals(item.getTextContent())) {
            if (updateCounter == 0) {
                oldXMlPort = valueNode.getNodeValue();
            }
            valueNode.setTextContent(peripheralServerPort);
            noOfVarGetUpdated++;
        }
        if (PERIPHERAL_SERVICE_ACTIVE_PROFILE.equals(item.getTextContent())) {
            if (updateCounter == 0) {
                oldXMlProfile = valueNode.getNodeValue();
            }
            valueNode.setTextContent(peripheralServerActiveProfile);
            noOfVarGetUpdated++;
        }
        if (INSTALLATION_LOCATION.equals(item.getTextContent())) {
            if (updateCounter == 0) {
                oldXMlInstalledLoc = valueNode.getNodeValue();
            }
            valueNode.setTextContent(peripheralServerInstalledLoc);
            noOfVarGetUpdated++;
        }
    }

    /**
     * This method undo the PeripheralUpdatePortInstalledLocProfileInXml Action
     * 
     * @return
     * @since 16-Apr-2019
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
     
            if (!initialException) {
                isUndo = true;
                updateXml();
        }
        return 0;
    }

    /**
     * This method validate the PeripheralUpdatePortInstalledLocProfileInXml Action
     * 
     * @return
     * @since 16-Apr-2019
     */
    @Override
    public boolean validateAction() {
        boolean checkupdatedValue = true;
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            File fileToBeValidated = new File(peripheralServerXmlPath);
            if (fileToBeValidated.exists()) {
                doc = docBuilder.parse(fileToBeValidated);
            } else {
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                "File not found at the specified location" + " " + fileToBeValidated, null);
            }
            // Get the root element
            Node serviceNode = doc.getElementsByTagName(initialTag).item(0);
            NodeList serviceChildNodes = serviceNode.getChildNodes();
            for (int i = 0; i < serviceChildNodes.getLength(); i++) {
                Node element = serviceChildNodes.item(i);
                if ("env".equals(element.getNodeName())) {
                    NamedNodeMap attributes = element.getAttributes();
                    
                    Node item = attributes.getNamedItem("name");
                    checkupdatedValue = validateXML(checkupdatedValue, attributes, item);
                }
            }
            if (noOfValuesToBeValidated != noOfVarGetUpdated) {
                checkupdatedValue = false;
            }
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                            exception.getCause());
        }
        return checkupdatedValue;
    }

    /**
     * This method validate the xml values
     * 
     * @return
     * @since 16-Apr-2019
     */
    private boolean validateXML(boolean checkupdatedValue, NamedNodeMap attributes, Node item) {
        for (String valueToBeUpdated : envValuesToBeUpdated) {
            if (item.getTextContent().equals(valueToBeUpdated)) {
                noOfValuesToBeValidated++;
                Node valueNode = attributes.getNamedItem("value");
                if (PERIPHERAL_SERVICE_PORT.equals(item.getTextContent())
                                && !peripheralServerPort.equals(valueNode.getNodeValue())) {
                    checkupdatedValue = false;
                    break;
                }
                if (PERIPHERAL_SERVICE_ACTIVE_PROFILE.equals(item.getTextContent())
                                && !peripheralServerActiveProfile.equals(valueNode.getNodeValue())) {
                    checkupdatedValue = false;
                    break;
                }
                if (INSTALLATION_LOCATION.equals(item.getTextContent())
                                && !peripheralServerInstalledLoc.equals(valueNode.getNodeValue())) {
                    checkupdatedValue = false;
                    break;
                }

            }
        }
        return checkupdatedValue;
    }

    /**
     * This method cleans up the intermediate file/folders created while performing
     * PeripheralUpdatePortInstalledLocProfileInXml action
     * 
     * @return
     * @since 16-Apr-2020
     */
    @Override
    public int cleanUp() {
        return 0;
    }

}
