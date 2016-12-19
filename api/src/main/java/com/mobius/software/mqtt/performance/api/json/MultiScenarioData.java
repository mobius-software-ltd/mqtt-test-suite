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

package com.mobius.software.mqtt.performance.api.json;

import java.util.List;

import com.mobius.software.mqtt.performance.api.data.ClientController;

public class MultiScenarioData
{
	private List<ClientController> controllers;
	private Long requestTimeout;
	private ScenarioRequest scenarioRequest;

	public MultiScenarioData()
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
		if (controllers == null || controllers.isEmpty())
			return false;

		if (scenarioRequest == null || !scenarioRequest.validate())
			return false;
		for (ClientController controller : controllers)
		{
			if (!controller.validate())
				return false;
			if (controller.getScenarioDelays().size() != scenarioRequest.getRequests().size())
				return false;
		}
		return requestTimeout != null;
	}
}
