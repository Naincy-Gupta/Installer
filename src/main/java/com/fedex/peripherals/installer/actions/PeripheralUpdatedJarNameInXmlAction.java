package com.fedex.peripherals.installer.actions;

import java.io.File;
import java.io.IOException;

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
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - PI 20.5 OTP Device Virtualization Implementation and Packaging<br/>
 * Description - This is the Action class used for updating the jar Name to xml
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 16-Apr-2020
 */
public class PeripheralUpdatedJarNameInXmlAction extends PeripheralSoftwareUpdateAction {
    private String peripheralServerLibPath;
    private PeripheralSoftwarePhasesErrorCode installationPhase;
    private File file;
    private Document doc;
    private String oldJarName;
    private String peripheralServerXmlPath;
    private boolean isUndo;
    private int noOfVarGetUpdated;
    private int noOfVarToBeValidated;
    private String libName;
    private int updateCounter = 0;
    private String initialTag = "service";
    private DocumentBuilderFactory docFactory;
    private boolean initialException = false;
    public static final String PERIPHERAL_SERVER_JAR = "PERIPHERAL-SERVER-JAR";
    private static final Logger logger = LogManager.getLogger(PeripheralUpdatedJarNameInXmlAction.class);

    /**
     * This method initialize the instance variable by reading the value from XML file
     * 
     * @since 16-Apr-2020
     */
    public void init() {
        installationPhase = this.getPeripheralSoftwareUpdatePhase();
        peripheralServerLibPath = (String) getProps().get(PeripheralSoftwareUpdateConstants.PERIPHERAL_SERVER_LIB_PATH);
        peripheralServerXmlPath =
                        (String) getProps().get(PeripheralSoftwareUpdateConstants.PERIPHERAL_SERVER_INSTALLER_PATH);
        noOfVarGetUpdated = 0;
        noOfVarToBeValidated = 0;
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
     * This method execute the PeripheralUpdatedJarNameInXmlAction Action
     * 
     * @return
     * @since 16-Apr-2020
     */
    @Override
    public int execute() {
        init();
            File folder = new File(peripheralServerLibPath);
            File[] fileList = folder.listFiles();
            for (File getFile : fileList) {
                if (getFile.getName().startsWith("rtl-peripherals")) {
                    libName = getFile.getName();
                    break;
                }
            }
            updateXML();
        return 0;
    }

    /**
     * @throws TransformerFactoryConfigurationError
     */
    private void updateXML() throws TransformerFactoryConfigurationError {
        try {
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        file = new File(peripheralServerXmlPath);
        if (file.exists()) {
            doc = docBuilder.parse(file);
            // Get the root element
            Node serviceNode = doc.getElementsByTagName(initialTag).item(0);
            NodeList serviceChildNode = serviceNode.getChildNodes();
            updateXMLValues(PERIPHERAL_SERVER_JAR, serviceChildNode);
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
                            "File not found at the specified location" + " " + file, null);
        }
      } catch (ParserConfigurationException | SAXException | IOException
                    | TransformerException transformerException) {
        logger.error(ExceptionUtils.getStackTrace(transformerException));
        throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                        transformerException.getMessage(), transformerException.getCause());
      }
    }

    /**
     * This method update the peripheral-server.xml env values like jar name
     * 
     * @return
     * @since 16-Apr-2020
     */
    private void updateXMLValues(String valueToBeUpdated, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node element = nodes.item(i);
            if ("env".equals(element.getNodeName())) {
                NamedNodeMap attributes = element.getAttributes();
                Node item = attributes.getNamedItem("name");
                if (item.getTextContent().equals(valueToBeUpdated)) {
                    Node valueNode = attributes.getNamedItem("value");
                    setValueToXml(item, valueNode);

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
    private void setValueToXml(Node item, Node valueNode) {
        if (!isUndo) {
            if (PERIPHERAL_SERVER_JAR.equals(item.getTextContent())) {
                if (updateCounter == 0) {
                    oldJarName = valueNode.getNodeValue();
                }
                valueNode.setTextContent(libName);
                noOfVarGetUpdated++;
            }
        } else {
            if (PERIPHERAL_SERVER_JAR.equals(item.getTextContent())) {
                valueNode.setTextContent(oldJarName);
            }
        }
    }

    /**
     * This method cleans up the intermediate file/folders created while performing PeripheralUpdatedJarNameInXmlAction
     * action
     * 
     * @return
     * @since 16-Apr-2020
     */
    @Override
    public int cleanUp() {
        return 0;
    }

    /**
     * This method undo the PeripheralUpdatedJarNameInXmlAction action
     * 
     * @return
     * @since 16-Apr-2020
     */
    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        try {
            if (!initialException) {
                DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                file = new File(peripheralServerXmlPath);
                if (file.exists()) {
                    doc = docBuilder.parse(file);
                    Node serviceNode = doc.getElementsByTagName(initialTag).item(0);
                    NodeList serviceChildNode = serviceNode.getChildNodes();
                    isUndo = true;
                    updateXMLValues(PERIPHERAL_SERVER_JAR, serviceChildNode);
                    // write the content into xml file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(file);
                    transformer.transform(source, result);
                } else {
                    logger.error("File {} not found at the specified location", file);
                    throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                    "File not found at the specified location" + " " + file, null);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException
                        | TransformerException transformerException) {
            logger.error(ExceptionUtils.getStackTrace(transformerException));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                            transformerException.getMessage(), transformerException.getCause());
        }
        return 0;
    }

    /**
     * This method validate the PeripheralUpdatedJarNameInXmlAction action
     * 
     * @return
     * @since 16-Apr-2020
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
                                "File not exists at the specified location" + " " + fileToBeValidated, null);
            }
            // Get the root element
            Node serviceNode = doc.getElementsByTagName(initialTag).item(0);
            NodeList serviceChildNode = serviceNode.getChildNodes();
            for (int i = 0; i < serviceChildNode.getLength(); i++) {
                Node element = serviceChildNode.item(i);
                if ("env".equals(element.getNodeName())) {
                    NamedNodeMap attributes = element.getAttributes();
                    Node item = attributes.getNamedItem("name");
                    if (item.getTextContent().equals(PERIPHERAL_SERVER_JAR)) {
                        Node valueNode = attributes.getNamedItem("value");
                        noOfVarToBeValidated++;
                        if (PERIPHERAL_SERVER_JAR.equals(item.getTextContent())
                                        && !libName.equals(valueNode.getNodeValue())) {
                            checkupdatedValue = false;
                            break;
                        }
                    }
                }
            }
            if (noOfVarToBeValidated != noOfVarGetUpdated) {
                checkupdatedValue = false;
            }
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            logger.error(ExceptionUtils.getStackTrace(exception));
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), exception.getMessage(),
                            exception.getCause());
        }
        return checkupdatedValue;
    }

}
