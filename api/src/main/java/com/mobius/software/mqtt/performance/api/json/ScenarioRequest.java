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
import java.util.UUID;

import com.mobius.software.mqtt.performance.api.data.ClientController;
import com.mobius.software.mqtt.performance.api.data.Scenario;

@SuppressWarnings("serial")
public class ScenarioRequest extends GenericJsonRequest
{
	private String requestUrl;
	private List<Scenario> requests;

	public ScenarioRequest()
	{

	}

	public List<Scenario> getRequests()
	{
		return requests;
	}

	public void setRequests(List<Scenario> requests)
	{
		this.requests = requests;
	}

	public boolean validate()
	{
		if (requests != null)
		{
			for (Scenario request : requests)
			{
				if (!request.validate())
					return false;
			}
		}
		return requests != null && !requests.isEmpty();
	}

	public ScenarioRequest fillData(ClientController controller)
	{
		for (int i = 0; i < requests.size(); i++)
		{
			Scenario scenario = requests.get(i);
			scenario.setId(UUID.randomUUID());
			Integer scenarioDelay = controller.getScenarioDelays().get(i);
			scenario.getProperties().setScenarioDelay(scenarioDelay);
			scenario.getProperties().setIdentifierRegex(controller.getIdentifierRegex());
			scenario.getProperties().setStartIdentifier(controller.getStartIdentifier());
		}
		return this;
	}

	public String retrieveURL()
	{
		return requestUrl;
	}

	public void updateURL(String url)
	{
		this.requestUrl = url;
	}
}
