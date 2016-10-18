package com.mobius.software.mqtt.client.api.json;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
public class GenericJsonResponse implements Serializable
{
	private String status;
	private String message;

	public GenericJsonResponse()
	{

	}

	public GenericJsonResponse(String status, String message)
	{
		this.status = status;
		this.message = message;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
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
