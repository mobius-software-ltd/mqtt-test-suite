package com.mobius.software.mqtt.client.api.data;

public class ConnectionProperties
{
	private String serverHostname;
	private Integer serverPort;
	private Long resendInterval;

	public ConnectionProperties()
	{

	}

	public ConnectionProperties(String serverHostname, Integer serverPort, Long resendInterval)
	{
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.resendInterval = resendInterval;
	}

	public boolean validate()
	{
		return serverHostname != null && serverPort != null && resendInterval != null;
	}

	public String getServerHostname()
	{
		return serverHostname;
	}

	public void setServerHostname(String serverHostname)
	{
		this.serverHostname = serverHostname;
	}

	public Integer getServerPort()
	{
		return serverPort;
	}

	public void setServerPort(Integer serverPort)
	{
		this.serverPort = serverPort;
	}

	public Long getResendInterval()
	{
		return resendInterval;
	}

	public void setResendInterval(Long resendInterval)
	{
		this.resendInterval = resendInterval;
	}
}
