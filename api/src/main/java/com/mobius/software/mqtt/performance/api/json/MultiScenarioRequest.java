package com.mobius.software.mqtt.performance.api.json;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mobius.software.mqtt.performance.api.data.ClientController;
import com.mobius.software.mqtt.performance.api.data.Scenario;
import com.mobius.software.mqtt.performance.api.data.SummaryData;
import com.mobius.software.mqtt.performance.commons.util.URLBuilder;

public class MultiScenarioRequest
{
	private List<ClientController> controllers;
	private Long requestTimeout;
	private ScenarioRequest scenarioRequest;

	public MultiScenarioRequest()
	{

	}

	public ScenarioRequest getScenarioRequest()
	{
		return scenarioRequest;
	}

	public void setScenarioRequest(ScenarioRequest scenarioRequest)
	{
		this.scenarioRequest = scenarioRequest;
	}

	public Long getRequestTimeout()
	{
		return requestTimeout;
	}

	public void setRequestTimeout(Long requestTimeout)
	{
		this.requestTimeout = requestTimeout;
	}

	public List<ClientController> getControllers()
	{
		return controllers;
	}

	public void setControllers(List<ClientController> controllers)
	{
		this.controllers = controllers;
	}

	public boolean validate()
	{
		return controllers != null && !controllers.isEmpty() && scenarioRequest != null && scenarioRequest.validate() && requestTimeout != null;
	}

	public Map<UUID, SummaryData> prepareSummaryData()
	{
		Map<UUID, SummaryData> summaryData = new HashMap<>();
		for (ClientController controller : controllers)
		{
			String baseURI = URLBuilder.buildBaseURI(controller.getHostname(), controller.getPort());
			for (Scenario scenario : scenarioRequest.getRequests())
				summaryData.put(scenario.getId(), SummaryData.initFrom(baseURI, scenario));
		}
		return summaryData;
	}
}
