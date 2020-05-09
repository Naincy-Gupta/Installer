package com.fedex.peripherals.installer.actions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;
import com.fedex.peripherals.installer.exception.PeripheralServerInstallerException;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwarePhasesErrorCode;
import com.fedex.peripherals.softwareupdate.constants.PeripheralSoftwareUpdateConstants;

public class PeripheralReadEnviromentVariableAction extends PeripheralSoftwareUpdateAction {
    private String enviromentVariable;
    private String enviromentVariableValue;
    private PeripheralSoftwarePhasesErrorCode installationPhase;

    @Override
    public int cleanUp() {
        return 0;
    }

    private void init() {
        enviromentVariable = getProps()
                        .getProperty(PeripheralSoftwareUpdateConstants.PERIPHERAL_INSTALLER_ENVIROMENT_VARIABLE);
        installationPhase = getPeripheralSoftwareUpdatePhase();
    }

    @Override
    public int execute() {
        init();
        Process process = null;
        ProcessBuilder processBuilder = new ProcessBuilder();
        try {

            processBuilder.redirectErrorStream(true);
            String[] commands = {"cmd.exe", "/c", "echo %" + enviromentVariable + "%"};
            processBuilder.command(commands);
            process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String commandOutput = null;
            while ((commandOutput = reader.readLine()) != null) {
                enviromentVariableValue = commandOutput;
            }
            if (StringUtils.isBlank(enviromentVariableValue)) {
                throw new PeripheralServerInstallerException(installationPhase.getErrorCode(),
                                "An exception has occured while starting peripheral server", null);
            }
        } catch (IOException ioException) {
            throw new PeripheralServerInstallerException(installationPhase.getErrorCode(), ioException.getMessage(),
                            ioException.getCause());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return 0;
    }

    @Override
    public int undoPeripheralSoftwareUpdateAction() {
        return 0;
    }

    @Override
    public boolean validateAction() {
        return false;
    }

    public String getEnviromentVariable() {
        return enviromentVariable;
    }

    public void setEnviromentVariable(String enviromentVariable) {
        this.enviromentVariable = enviromentVariable;
    }

    public String getEnviromentVariableValue() {
        return enviromentVariableValue;
    }

    public void setEnviromentVariableValue(String enviromentVariableValue) {
        this.enviromentVariableValue = enviromentVariableValue;
    }


}
