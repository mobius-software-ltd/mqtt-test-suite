package com.mobius.software.mqtt.performance.api.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mobius.software.mqtt.performance.api.json.ScenarioRequest;

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

public class ClientController
{
	private String hostname;
	private Integer port;
	private String identifierRegex;
	private Integer startIdentifier;
	private Integer scenarioDelay;

	public ClientController()
	{

	}

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public Integer getPort()
	{
		return port;
	}

	public void setPort(Integer port)
	{
		this.port = port;
	}

	public String getIdentifierRegex()
	{
		return identifierRegex;
	}

	public void setIdentifierRegex(String identifierRegex)
	{
		this.identifierRegex = identifierRegex;
	}

	public Integer getStartIdentifier()
	{
		return startIdentifier;
	}

	public void setStartIdentifier(Integer startIdentifier)
	{
		this.startIdentifier = startIdentifier;
	}

	public Integer getScenarioDelay()
	{
		return scenarioDelay;
	}

	public void setScenarioDelay(Integer scenarioDelay)
	{
		this.scenarioDelay = scenarioDelay;
	}

	public boolean validate()
	{
		if (port == null || port < 1 || port > 65535)
			return false;

		if (scenarioDelay == null || scenarioDelay < 0)
			return false;

		return hostname != null && identifierRegex != null && startIdentifier != null;
	}

	public List<Scenario> translateScenarioRequests(ScenarioRequest scenarioRequest)
	{
		List<Scenario> scenarios = new ArrayList<>();
		for (Scenario scenario : scenarioRequest.getRequests())
		{
			scenario.setId(UUID.randomUUID());
			scenario.getProperties().setScenarioDelay(scenarioDelay);
			scenario.getProperties().setIdentifierRegex(identifierRegex);
			scenario.getProperties().setStartIdentifier(startIdentifier);
			scenarios.add(scenario);
		}
		return scenarios;
	}
}
