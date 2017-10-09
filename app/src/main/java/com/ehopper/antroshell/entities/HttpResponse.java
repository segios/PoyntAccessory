package com.ehopper.antroshell.entities;

public class HttpResponse
{
	private int responseCode;
	private String responseMessage;
	private String errorMessage;
	private String data;
	
	public int getResponseCode()
	{
		return responseCode;
	}
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
	}
	public String getResponseMessage()
	{
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage)
	{
		this.responseMessage = responseMessage;
	}
	public String getErrorMessage()
	{
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}
	public String getData()
	{
		return data;
	}
	public void setData(String data)
	{
		this.data = data;
	}
}
