package com.mobius.software.mqtt.client.api.data;

import java.util.List;

import com.mobius.software.mqtt.client.util.CommandParser;

public class Command
{
	private CommandType type;
	private Long sendTime;
	private List<Property> commandProperties;

	public Command()
	{

	}

	public Command(CommandType type, Long sendTime, List<Property> commandProperties)
	{
		this.type = type;
		this.sendTime = sendTime;
		this.commandProperties = commandProperties;
	}

	public CommandType getType()
	{
		return type;
	}

	public void setType(CommandType type)
	{
		this.type = type;
	}

	public Long getSendTime()
	{
		return sendTime;
	}

	public void setSendTime(Long sendTime)
	{
		this.sendTime = sendTime;
	}

	public List<Property> getCommandProperties()
	{
		return commandProperties;
	}

	public void setCommandProperties(List<Property> commandProperties)
	{
		this.commandProperties = commandProperties;
	}

	public boolean validate()
	{
		return type != null && sendTime != null && CommandParser.validate(this);
	}
}
