package com.mobius.software.mqtt.performance.api.data;

/**
 * Mobius Software LTD
 * Copyright 2015-2016, Mobius Software LTD
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ConnectionContext
{
	private Boolean cleanSession;
	private Integer keepalive;
	private String clientID;

	private SocketAddress clientAddress;
	private SocketAddress serverAddress;
	private Long resendInterval;

	public ConnectionContext(InetSocketAddress serverAddress, Long resendInterval)
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
