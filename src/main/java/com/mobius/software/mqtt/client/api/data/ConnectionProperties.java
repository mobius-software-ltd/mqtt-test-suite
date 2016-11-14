package com.mobius.software.mqtt.client.api.data;

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

public class ConnectionProperties
{
	private String serverHostname;
	private Integer serverPort;
	private Long resendInterval;
	private Long minPingInterval;

	public ConnectionProperties()
	{

	}

	public ConnectionProperties(String serverHostname, Integer serverPort, Long resendInterval, Long minPingInterval)
	{
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.resendInterval = resendInterval;
		this.minPingInterval = minPingInterval;
	}

	public boolean validate()
	{
		return serverHostname != null && serverPort != null && resendInterval != null && minPingInterval != null;
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

	public Long getMinPingInterval()
	{
		return minPingInterval;
	}

	public void setMinPingInterval(Long minPingInterval)
	{
		this.minPingInterval = minPingInterval;
	}
}
