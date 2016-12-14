package com.mobius.software.mqtt.performance.api.data;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.performance.commons.data.CommandCounter;
import com.mobius.software.mqtt.performance.commons.data.CommandType;
import com.mobius.software.mqtt.performance.commons.data.Counter;
import com.mobius.software.mqtt.performance.commons.data.Direction;

public class ClientReport
{
	private String identifier;
	private Integer unfinishedCommands;
	private List<CommandCounter> commandCounters;
	private List<Counter> duplicateCounters;
	private List<ErrorReport> errors;

	public ClientReport()
	{

	}

	public ClientReport(String identifier, Integer unfinishedCommands, List<CommandCounter> commandCounters, List<Counter> duplicateCounter, List<ErrorReport> errors)
	{
		super();
		this.identifier = identifier;
		this.unfinishedCommands = unfinishedCommands;
		this.commandCounters = commandCounters;
		this.duplicateCounters = duplicateCounter;
		this.errors = errors;
	}

	public static ClientReport valueOf(IdentityReport report)
	{
		List<CommandCounter> commandCounters = new ArrayList<>();
		for (Entry<MessageType, AtomicInteger> entry : report.getInPacketCounters().entrySet())
			if (entry.getValue().get() > 0)
				commandCounters.add(new CommandCounter(CommandType.fromMessageType(entry.getKey()), entry.getValue().get(), Direction.INCOMING));
		for (Entry<MessageType, AtomicInteger> entry : report.getOutPacketCounters().entrySet())
			if (entry.getValue().get() > 0)
				commandCounters.add(new CommandCounter(CommandType.fromMessageType(entry.getKey()), entry.getValue().get(), Direction.OUTGOING));
		Counter inDup = new Counter(report.getInDuplicates().get(), Direction.INCOMING);
		Counter outDup = new Counter(report.getOutDuplicates().get(), Direction.OUTGOING);
		List<Counter> duplicateCounters = Arrays.asList(new Counter[]
		{ inDup, outDup });
		ClientReport clientReport = new ClientReport(report.getClientID(), report.getUnfinishedCommands(), commandCounters, duplicateCounters, report.getErrors());
		return clientReport;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public Integer getUnfinishedCommands()
	{
		return unfinishedCommands;
	}

	public void setUnfinishedCommands(Integer unfinishedCommands)
	{
		this.unfinishedCommands = unfinishedCommands;
	}

	public List<CommandCounter> getCommandCounters()
	{
		return commandCounters;
	}

	public void setCommandCounters(List<CommandCounter> commandCounters)
	{
		this.commandCounters = commandCounters;
	}

	public List<Counter> getDuplicateCounters()
	{
		return duplicateCounters;
	}

	public void setDuplicateCounters(List<Counter> duplicateCounters)
	{
		this.duplicateCounters = duplicateCounters;
	}

	public List<ErrorReport> getErrors()
	{
		return errors;
	}

	public void setErrors(List<ErrorReport> errors)
	{
		this.errors = errors;
	}
}
