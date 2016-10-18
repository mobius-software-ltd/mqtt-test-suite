package com.mobius.software.mqtt.client.api.data;

import java.net.SocketAddress;

public class ConnectionContext
{
	private Boolean cleanSession;
	private Integer keepalive;
	private String clientID;

	private SocketAddress clientAddress;
	private SocketAddress serverAddress;
	private Long resendInterval;

	public ConnectionContext(SocketAddress serverAddress, Long resendInterval)
	{
		this.serverAddress = serverAddress;
		this.resendInterval = resendInterval;
	}

	public Boolean getCleanSession()
	{
		return cleanSession;
	}

	public Integer getKeepalive()
	{
		return keepalive;
	}

	public Long getResendInterval()
	{
		return resendInterval;
	}

	public String getClientID()
	{
		return clientID;
	}

	public SocketAddress localAddress()
	{
		return clientAddress;
	}

	public void updateLocalAddress(SocketAddress clientAddress)
	{
		this.clientAddress = clientAddress;
	}

	public SocketAddress remoteAddress()
	{
		return serverAddress;
	}

	public void update(Boolean cleanSession, Integer keepalive, String clientID)
	{
		this.cleanSession = cleanSession;
		this.keepalive = keepalive;
		this.clientID = clientID;
	}
}
