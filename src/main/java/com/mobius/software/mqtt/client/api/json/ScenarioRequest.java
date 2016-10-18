package com.mobius.software.mqtt.client.api.json;

import java.util.List;

import com.mobius.software.mqtt.client.api.data.Scenario;

public class ScenarioRequest
{
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
}
