package com.fedex.peripherals.installer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fedex.peripherals.installer.actions.PeripheralInstallationVerificationAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.foundation.ActionStatusInfo;

/**
 * Copyright (c) 2019 FedEx. All Rights Reserved.<br/>
 * 
 * Theme - Core Retail Peripheral Services<br/>
 * Feature - Peripheral Services - Build and package for phased retail deployment<br/>
 * Description - This class is used to execute every action defined
 * 
 * @author Naincy Gupta [3777204]
 * @version 0.0.1
 * @since 4-Oct-2019
 */
public class PeripheralSoftwareUpdate {

    private int actionStackPointer = -1;
    private List<PeripheralSoftwareUpdateAction> actionStack = new LinkedList<>();
    private boolean performFurtherActions = true;
    private int statusCode;
    private String serverInformation;
    private boolean actionRequired = false;
    private static final Logger logger = LogManager.getLogger(PeripheralSoftwareUpdate.class);

    /**
     * This method is used to execute the Action
     * 
     * @param action
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public boolean executeAction(PeripheralSoftwareUpdateAction action) {
        logger.traceEntry("executeAction method of PeripheralSoftwareUpdate class");
        deleteElementsAfterPointer(actionStackPointer);
        boolean actionStatus = false;
        statusCode = 0;
        try {
            logger.info("Calls the execute method of particular action based on value of performFurtherActions");
            if (performFurtherActions && !(action instanceof PeripheralInstallationVerificationAction)) {
                statusCode = action.execute();
                // if action executed successfully increment the action pointer
                actionStackPointer++;
                actionStack.add(actionStackPointer, action);
                logger.info("Calls the validation method of particular action if execute method exeutes successfully");
                actionStatus = validateAction();
                if (!actionStatus) {
                    if (action.getRetryCount() > 0) {
                        actionStatus = redoPeripheralSoftwareUpdateActon();
                    } else {
                        undoPeripheralSoftwareUpdateActon();
                    }
                }
            } else if (performFurtherActions && action instanceof PeripheralInstallationVerificationAction) {
                // this block of code is executed when the action to be executed is PeripheralInstallationVerificationAction
                PeripheralInstallationVerificationAction peripheralInstallationVerificationAction =
                                (PeripheralInstallationVerificationAction) action;
                statusCode = peripheralInstallationVerificationAction.execute();
                serverInformation = peripheralInstallationVerificationAction.getServerInformation();
                actionStackPointer++;
                actionStack.add(actionStackPointer, peripheralInstallationVerificationAction);
                if (serverInformation != null) {
                    actionStatus = validateAction();
                } else {
                    if (peripheralInstallationVerificationAction.getRetryCount() > 0) {
                        actionStatus = redoPeripheralSoftwareUpdateActon();
                    } else {
                        undoPeripheralSoftwareUpdateActon();
                    }
                }
            }
        } catch (PeripheralServerInstallerException peripheralServerInstallerException) {
            logger.debug("Calling the undo action");
            actionStackPointer++;
            actionStack.add(actionStackPointer, action);
            statusCode = peripheralServerInstallerException.getErrorCode();
            try {
                undoPeripheralSoftwareUpdateActon();
            } catch (PeripheralServerInstallerException psie) {
                logger.debug("Exception while undoing the action");
                statusCode = psie.getErrorCode();
                setActionRequired(true);
            }
        }
        this.setStatusCode(statusCode);
        return logger.traceExit(actionStatus);
    }

    /**
     * This method used to remove the action from the stack based on the value of undo pointer
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    private void deleteElementsAfterPointer(int undoRedoPointer) {
        if (actionStack.isEmpty())
            return;
        for (int i = actionStack.size() - 1; i > undoRedoPointer; i--) {
            actionStack.remove(i);
        }
    }

    /**
     * This method undo the Action
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public void undoPeripheralSoftwareUpdateActon() {
        logger.traceEntry("undoPeripheralSoftwareUpdateActon method of PeripheralSoftwareUpdate class");
        PeripheralSoftwareUpdateAction action = actionStack.get(actionStackPointer);
        statusCode = statusCode == 0 ? 500 : statusCode;
        logger.info("status code in undo method: {}",statusCode);
        if (action.isRollback() && action.isExitOnFail()) {
            logger.debug("setting the setPerform Further Action action.isExitOnFail()");
            setPerformFurtherActions(!action.isExitOnFail());
            while (!actionStack.isEmpty()) {
                action = actionStack.get(actionStackPointer);
                action.undoPeripheralSoftwareUpdateAction();
                actionStack.remove(actionStackPointer);
                actionStackPointer--;
            }
        } else if (action.isRollback() && !action.isExitOnFail()) {
            logger.debug("setting the setPerform Further Action");
            setPerformFurtherActions(!action.isExitOnFail());
            actionRequired = true;
            action = actionStack.get(actionStackPointer);
            action.undoPeripheralSoftwareUpdateAction();
            actionStack.remove(actionStackPointer);
            actionStackPointer--;
        } else if (!action.isRollback() && action.isExitOnFail()) {
            logger.debug("setting the setPerform Further Action to action.isExitOnFail() value");
            setPerformFurtherActions(!action.isExitOnFail());
            actionStack.remove(actionStackPointer);
            actionStackPointer--;
            while (!actionStack.isEmpty()) {
                action = actionStack.get(actionStackPointer);
                action.undoPeripheralSoftwareUpdateAction();
                actionStack.remove(actionStackPointer);
                actionStackPointer--;
            }
        } else {
            actionRequired = true;
        }
        logger.trace("Exit undoPeripheralSoftwareUpdateActon()");
    }

    /**
     * This method retries the action execution until number of retries provided
     * 
     * @return void
     * @since Oct 4, 2019
     * @version 0.0.1
     */
    public boolean redoPeripheralSoftwareUpdateActon() {
        boolean actionStatus = false;
        logger.traceEntry("redoPeripheralSoftwareUpdateActon method of PeripheralSoftwareUpdate class");
        PeripheralSoftwareUpdateAction action = actionStack.get(actionStackPointer);
        if (!(action instanceof PeripheralInstallationVerificationAction)) {
            // fetch the value of retry count for the particular action and calls the execute method
            int retryCount = action.getRetryCount();
            action.setRetryCount(--retryCount);
            action.undoPeripheralSoftwareUpdateAction();
            action.execute();
            // if execute method executed successfully calls the validation method of particular action
            actionStatus = validateAction();
            if (!actionStatus) {
                if (action.getRetryCount() > 0) {
                    redoPeripheralSoftwareUpdateActon();
                } else {
                    undoPeripheralSoftwareUpdateActon();
                }
            }
        } else if (action instanceof PeripheralInstallationVerificationAction) {
            // this block of code is executed when the action to be executed is PeripheralInstallationVerificationAction
            PeripheralInstallationVerificationAction peripheralInstallationVerificationAction =
                            (PeripheralInstallationVerificationAction) action;
            int retryCount = peripheralInstallationVerificationAction.getRetryCount();
            peripheralInstallationVerificationAction.setRetryCount(--retryCount);
            peripheralInstallationVerificationAction.undoPeripheralSoftwareUpdateAction();
            peripheralInstallationVerificationAction.execute();
            serverInformation = peripheralInstallationVerificationAction.getServerInformation();
            if (serverInformation != null) {
                actionStatus = validateAction();
            } else {
                if (peripheralInstallationVerificationAction.getRetryCount() > 0) {
                    actionStatus = redoPeripheralSoftwareUpdateActon();
                } else {
                    undoPeripheralSoftwareUpdateActon();
                }
            }
        }
        return logger.traceExit("Exit redoPeripheralSoftwareUpdateActon()", actionStatus);
    }

    /**
     * This method validate the action performed
     * 
     * @return boolean
     * @since Oct 9, 2019
     * @version 0.0.1
     */
    public boolean validateAction() {
        return actionStack.get(actionStackPointer).validateAction();
    }

    /**
     * This method clean up the action performed and returns List of {@link ActionStatusInfo}
     * 
     * @return {@code List<ActionStatusInfo>} List of {@link ActionStatusInfo}
     * @since Oct 9, 2019
     */
    public List<ActionStatusInfo> cleanUp() {
        boolean isCleanUpSuccess = true;
        List<ActionStatusInfo> cleanUpActions = new ArrayList<>();
        logger.info("Calls the cleanUp method of PeripheralSoftwareUpdate class");
        PeripheralSoftwareUpdateAction peripheralSoftwareUpdateAction;
        while (!actionStack.isEmpty()) {
            peripheralSoftwareUpdateAction = actionStack.get(actionStackPointer);
            try {
                peripheralSoftwareUpdateAction.cleanUp();
            } catch (PeripheralServerInstallerException peripheralServerInstallerException) {
                logger.error(ExceptionUtils.getStackTrace(peripheralServerInstallerException));
                isCleanUpSuccess = false;
            }
            ActionStatusInfo actionStatusInfo = new ActionStatusInfo(peripheralSoftwareUpdateAction.getName(),
                            peripheralSoftwareUpdateAction.getDescription(), isCleanUpSuccess);
            cleanUpActions.add(actionStatusInfo);
            actionStack.remove(actionStackPointer);
            actionStackPointer--;
        }
        return cleanUpActions;
    }


    public boolean isPerformFurtherActions() {
        return performFurtherActions;
    }

    public void setPerformFurtherActions(boolean performFurtherActions) {
        this.performFurtherActions = performFurtherActions;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(boolean actionRequired) {
        this.actionRequired = actionRequired;
    }

    public int getActionStackPointer() {
        return actionStackPointer;
    }

    public void setActionStackPointer(int actionStackPointer) {
        this.actionStackPointer = actionStackPointer;
    }

    public List<PeripheralSoftwareUpdateAction> getActionStack() {
        return actionStack;
    }

    public void setActionStack(List<PeripheralSoftwareUpdateAction> actionStack) {
        this.actionStack = actionStack;
    }

	public String getServerInformation() {
		return serverInformation;
	}

	public void setServerInformation(String serverInformation) {
		this.serverInformation = serverInformation;
	}



}
