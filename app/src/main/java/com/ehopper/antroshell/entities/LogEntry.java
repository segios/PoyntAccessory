package com.ehopper.antroshell.entities;

import java.util.Date;

public class LogEntry
{
	private LogLevel 	logLevel;
	private String 		loggerName;
	private String 		title;
	private String 		correlationId;
	private String 		tenantId;
	private Date 		timestamp;
	private String	 	message;

	public LogLevel getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel)
	{
		this.logLevel = logLevel;
	}
	
	public String getLoggerName()
	{
		return loggerName;
	}

	public void setLoggerName(String loggerName)
	{
		this.loggerName = loggerName;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getCorrelationId()
	{
		return correlationId;
	}

	public void setCorrelationId(String correlationId)
	{
		this.correlationId = correlationId;
	}

	public String getTenantId()
	{
		return tenantId;
	}

	public void setTenantId(String tenantId)
	{
		this.tenantId = tenantId;
	}

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
