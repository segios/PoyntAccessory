package com.ehopper.antroshell.entities;

import com.starmicronics.stario.StarPrinterStatus;

public class PrinterStatus
{
	private boolean isReady;
	private StarPrinterStatus additionalStatus;
	
	public boolean isReady()
	{
		return isReady;
	}
	public void setReady(boolean isReady)
	{
		this.isReady = isReady;
	}
	
	public StarPrinterStatus getAdditionalStatus()
	{
		return additionalStatus;
	}
	public void setAdditionalStatus(StarPrinterStatus additionalStatus)
	{
		this.additionalStatus = additionalStatus;
	}

	public boolean drawerIsOpen;
	public boolean isOffline;
	public boolean coverIsOpen;
	public boolean feeding;
	public boolean printerError;
	public boolean cutterError;
	public boolean paperNearEnd;
	public boolean paperEnd;

}
