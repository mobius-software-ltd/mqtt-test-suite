package com.mobius.software.mqtt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.client.api.data.CommandType;
import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class ClientReport
{
	private String identifier;
	private List<Counter> counters;
	private Integer inDuplicates;
	private Integer outDuplicates;
	private List<ErrorReport> errors;

	public ClientReport()
	{

	}

	public ClientReport(String identifier, List<Counter> counters, Integer inDuplicates, Integer outDuplicates, List<ErrorReport> errors)
	{
		this.identifier = identifier;
		this.counters = counters;
		this.inDuplicates = inDuplicates;
		this.outDuplicates = outDuplicates;
		this.errors = errors;
	}

	public static ClientReport valueOf(IdentityReport report)
	{
		List<Counter> counters = new ArrayList<>();
		for (Entry<MessageType, AtomicInteger> entry : report.getInPacketCounters().entrySet())
			if (entry.getValue().get() > 0)
				counters.add(new Counter(CommandType.fromMessageType(entry.getKey()), entry.getValue().get(), true));
		for (Entry<MessageType, AtomicInteger> entry : report.getOutPacketCounters().entrySet())
			if (entry.getValue().get() > 0)
				counters.add(new Counter(CommandType.fromMessageType(entry.getKey()), entry.getValue().get(), false));
		return new ClientReport(report.getClientID(), counters, report.getInDuplicates().get(), report.getOutDuplicates().get(), report.getErrors());
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public List<Counter> getCounters()
	{
		return counters;
	}

	public void setCounters(List<Counter> counters)
	{
		this.counters = counters;
	}

	public Integer getInDuplicates()
	{
		return inDuplicates;
	}

	public void setInDuplicates(Integer inDuplicates)
	{
		this.inDuplicates = inDuplicates;
	}

	public Integer getOutDuplicates()
	{
		return outDuplicates;
	}

	public void setOutDuplicates(Integer outDuplicates)
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
}
