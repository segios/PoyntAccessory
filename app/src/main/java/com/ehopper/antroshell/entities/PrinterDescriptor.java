package com.ehopper.antroshell.entities;

public class PrinterDescriptor  implements java.io.Serializable
{
	private String address;
	private String fullAddress;
	private String modelName;
	private double scale = 1;
	private int textLength = 30;

	private boolean isReady;

	private float printableAreaWidth;
	private int paperSize;
    private String paperSizeName;
    private Boolean paperSizeNotSwitchable;
	private float dpi;
	private float dpiX;
	private float dpiY;
	private PrinterManaufacture printerManaufacture;
	private PrinterType printerType;


	public PrinterManaufacture getPrinterManaufacture() {
		return printerManaufacture;
	}

	public void setPrinterManaufacture(PrinterManaufacture printerManaufacture) {
		this.printerManaufacture = printerManaufacture;
	}

	public PrinterType getPrinterType() {
		return printerType;
	}

	public void setPrinterType(PrinterType printerType) {
		this.printerType = printerType;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getFullAddress()
	{
		return fullAddress;
	}

	public void setFullAddress(String fullAddress)
	{
		this.fullAddress = fullAddress;
	}

	public float getPrintableAreaWidth()
	{
		return printableAreaWidth;
	}

	public void setPrintableAreaWidth(float printableAreaWidth)
	{
		this.printableAreaWidth = printableAreaWidth;
	}

	public float getDpiX()
	{
		return dpiX;
	}

	public void setDpiX(float dpiX)
	{
		this.dpiX = dpiX;
	}

	public float getDpiY()
	{
		return dpiY;
	}

	public void setDpiY(float dpiY)
	{
		this.dpiY = dpiY;
	}

	public float getDpi()
	{
		return dpi;
	}

	public void setDpi(float dpi)
	{
		this.dpi = dpi;
	}

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName(String modelName)
	{
		this.modelName = modelName;
	}

	public boolean isReady()
	{
		return isReady;
	}

	public void setReady(boolean isReady)
	{
		this.isReady = isReady;
	}

	public int getPaperSize()
	{
		return paperSize;
	}

	public void setPaperSize(int paperSize)
	{
		this.paperSize = paperSize;
	}

    public String getPaperSizeName()
    {
        return paperSizeName;
    }

    public void setPaperSizeName(String paperSizeName)
    {
        this.paperSizeName = paperSizeName;
    }

    public Boolean getPaperSizeNotSwitchable()
    {
        return paperSizeNotSwitchable;
    }

    public void setPaperSizeNotSwitchable(Boolean paperSizeNotSwitchable)
    {
        this.paperSizeNotSwitchable = paperSizeNotSwitchable;
    }

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public int getTextLength() {
		return textLength;
	}

	public void setTextLength(int textLength) {
		this.textLength = textLength;
	}
}
