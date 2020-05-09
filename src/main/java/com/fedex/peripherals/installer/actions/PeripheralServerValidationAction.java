package com.fedex.peripherals.installer.actions;

import com.fedex.peripherals.installer.PeripheralSoftwareUpdateAction;

public class PeripheralServerValidationAction extends PeripheralSoftwareUpdateAction {
	
	@Override
	public int execute() {
		return 0;
	}
	@Override
	public boolean validateAction() {
		return false;
	}


	
	@Override
	public int undoPeripheralSoftwareUpdateAction() {
		return 0;
	}

	
	@Override
	public int cleanUp() {
		return 0;
	}
}
