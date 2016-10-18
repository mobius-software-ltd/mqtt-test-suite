package com.mobius.software.mqtt.client.api.data;

import java.util.List;
import java.util.UUID;

public class Scenario
{
	private UUID id;
	private ConnectionProperties properties;
	private Integer count;
	private Integer threshold;
	private Boolean continueOnError;
	private List<Command> commands;

	public Scenario()
	{

	}

	public boolean validate()
	{
		if (commands != null)
		{
			for (Command command : commands)
				if (!command.validate())
					return false;
		}
		return id != null && properties != null && properties.validate() && count != null && threshold != null && continueOnError != null && commands != null && !commands.isEmpty();
	}

	public UUID getId()
	{
		return id;
	}

	public void setId(UUID id)
	{
		this.id = id;
	}

	public ConnectionProperties getProperties()
	{
		return properties;
	}

	public void setProperties(ConnectionProperties properties)
	{
		this.properties = properties;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public Integer getThreshold()
	{
		return threshold;
	}

	public void setThreshold(Integer threshold)
	{
		this.threshold = threshold;
	}

	public List<Command> getCommands()
	{
		return commands;
	}

	public void setCommands(List<Command> commands)
	{
		this.commands = commands;
	}

	public Boolean getContinueOnError()
	{
		return continueOnError;
	}

	public void setContinueOnError(Boolean continueOnError)
	{
		this.continueOnError = continueOnError;
	}
}
