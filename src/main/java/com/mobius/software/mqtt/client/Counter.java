package com.mobius.software.mqtt.client;

import com.mobius.software.mqtt.client.api.data.CommandType;

public class Counter
{
	private CommandType command;
	private Integer count;
	private Boolean isIncoming;

	public Counter()
	{

	}

	public Counter(CommandType command, Integer count, Boolean isIncoming)
	{
		this.command = command;
		this.count = count;
		this.isIncoming = isIncoming;
	}

	public CommandType getCommand()
	{
		return command;
	}

	public void setCommand(CommandType command)
	{
		this.command = command;
	}

	public Integer getCount()
	{
		return count;
	}

	public void setCount(Integer count)
	{
		this.count = count;
	}

	public Boolean getIsIncoming()
	{
		return isIncoming;
	}

	public void setIsIncoming(Boolean isIncoming)
	{
		this.isIncoming = isIncoming;
	}
}
