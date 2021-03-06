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

package com.mobius.software.mqtt.performance.controller.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.performance.commons.data.*;

public class IdentityReport
{
	private String clientID;

	private ConcurrentHashMap<MessageType, AtomicInteger> inPacketCounters = new ConcurrentHashMap<>();
	private ConcurrentHashMap<MessageType, AtomicInteger> outPacketCounters = new ConcurrentHashMap<>();

	private int unfinishedCommands;

	private AtomicInteger inDuplicates = new AtomicInteger();
	private AtomicInteger outDuplicates = new AtomicInteger();

	private List<ErrorReport> errors = new ArrayList<>();

	public IdentityReport(String clientID)
	{
		this.clientID = clientID;
		for (MessageType type : MessageType.values())
		{
			inPacketCounters.put(type, new AtomicInteger(0));
			outPacketCounters.put(type, new AtomicInteger(0));
		}
	}

	public int getUnfinishedCommands()
	{
		return unfinishedCommands;
	}

	public void setUnfinishedCommands(int unfinishedCommands)
	{
		this.unfinishedCommands = unfinishedCommands;
	}

	public void countIn(MessageType type)
	{
		inPacketCounters.get(type).incrementAndGet();
	}

	public void countOut(MessageType type)
	{
		outPacketCounters.get(type).incrementAndGet();
	}

	public void countDuplicateIn()
	{
		inDuplicates.incrementAndGet();
	}

	public void countDuplicateOut()
	{
		outDuplicates.incrementAndGet();
	}

	public String getClientID()
	{
		return clientID;
	}

	public void setClientID(String clientID)
	{
		this.clientID = clientID;
	}

	public ConcurrentHashMap<MessageType, AtomicInteger> getInPacketCounters()
	{
		return inPacketCounters;
	}

	public void setInPacketCounters(ConcurrentHashMap<MessageType, AtomicInteger> inPacketCounters)
	{
		this.inPacketCounters = inPacketCounters;
	}

	public ConcurrentHashMap<MessageType, AtomicInteger> getOutPacketCounters()
	{
		return outPacketCounters;
	}

	public void setOutPacketCounters(ConcurrentHashMap<MessageType, AtomicInteger> outPacketCounters)
	{
		this.outPacketCounters = outPacketCounters;
	}

	public AtomicInteger getInDuplicates()
	{
		return inDuplicates;
	}

	public void setInDuplicates(AtomicInteger inDuplicates)
	{
		this.inDuplicates = inDuplicates;
	}

	public AtomicInteger getOutDuplicates()
	{
		return outDuplicates;
	}

	public void setOutDuplicates(AtomicInteger outDuplicates)
	{
		this.outDuplicates = outDuplicates;
	}

	public List<ErrorReport> getErrors()
	{
		return errors;
	}

	public void setErrors(List<ErrorReport> errors)
	{
		this.errors = errors;
	}

	public void reportError(ErrorType type, String message)
	{
		errors.add(new ErrorReport(type, message, System.currentTimeMillis()));
	}

	public ClientReport translate()
	{
		List<CommandCounter> commandCounters = new ArrayList<>();
		for (Entry<MessageType, AtomicInteger> entry : inPacketCounters.entrySet())
			if (entry.getValue().get() > 0)
				commandCounters.add(new CommandCounter(CommandType.fromMessageType(entry.getKey()), entry.getValue().get(), Direction.INCOMING));
		for (Entry<MessageType, AtomicInteger> entry : outPacketCounters.entrySet())
			if (entry.getValue().get() > 0)
				commandCounters.add(new CommandCounter(CommandType.fromMessageType(entry.getKey()), entry.getValue().get(), Direction.OUTGOING));
		Counter inDup = new Counter(inDuplicates.get(), Direction.INCOMING);
		Counter outDup = new Counter(outDuplicates.get(), Direction.OUTGOING);
		List<Counter> duplicateCounters = Arrays.asList(new Counter[]
		{ inDup, outDup });
		ClientReport clientReport = new ClientReport(clientID, unfinishedCommands, commandCounters, duplicateCounters, errors);
		return clientReport;
	}
}
