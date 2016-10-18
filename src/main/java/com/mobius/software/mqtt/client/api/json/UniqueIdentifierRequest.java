package com.mobius.software.mqtt.client.api.json;

import java.util.UUID;

public class UniqueIdentifierRequest
{
	private UUID id;

	public UniqueIdentifierRequest()
	{

	}

	public UniqueIdentifierRequest(UUID id)
	{
		this.id = id;
	}

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

	public boolean validate()
	{
		return id != null;
	}
}
