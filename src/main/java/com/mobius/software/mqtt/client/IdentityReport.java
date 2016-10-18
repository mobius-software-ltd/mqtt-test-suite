package com.mobius.software.mqtt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class IdentityReport
{
	private String clientID;

	private ConcurrentHashMap<MessageType, AtomicInteger> inPacketCounters = new ConcurrentHashMap<>();
	private ConcurrentHashMap<MessageType, AtomicInteger> outPacketCounters = new ConcurrentHashMap<>();

	private AtomicInteger inDuplicates = new AtomicInteger();
	private AtomicInteger outDuplicates = new AtomicInteger();

	private List<ErrorReport> errors = new ArrayList<>();

	public IdentityReport()
	{
		for (MessageType type : MessageType.values())
		{
			inPacketCounters.put(type, new AtomicInteger(0));
			outPacketCounters.put(type, new AtomicInteger(0));
		}
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

	public void reportError(String type, String message)
	{
		errors.add(new ErrorReport(type, message, System.currentTimeMillis()));
	}
}
