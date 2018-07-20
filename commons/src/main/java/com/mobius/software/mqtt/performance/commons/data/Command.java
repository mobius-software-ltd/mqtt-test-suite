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

package com.mobius.software.mqtt.performance.commons.data;

import java.util.List;

import com.mobius.software.mqtt.performance.commons.util.CommandParser;

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

	@Override
	public String toString()
	{
		return "Command [type=" + type + ", sendTime=" + sendTime + ", commandProperties=" + commandProperties + "]";
	}
}
