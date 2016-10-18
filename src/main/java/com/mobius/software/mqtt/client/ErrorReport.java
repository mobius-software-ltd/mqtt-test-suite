package com.mobius.software.mqtt.client;

public class ErrorReport
{
	private String type;
	private String message;
	private Long timestamp;

	public ErrorReport()
	{

	}

	public ErrorReport(String type, String message, Long timestamp)
	{
		this.type = type;
		this.message = message;
		this.timestamp = timestamp;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public Long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Long timestamp)
	{
		this.timestamp = timestamp;
	}

}
