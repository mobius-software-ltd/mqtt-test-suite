package com.mobius.software.mqtt.client.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mobius.software.mqtt.client.Counter;
import com.mobius.software.mqtt.client.api.data.CommandType;

public class CounterComparator
{
	static AtomicBoolean once = new AtomicBoolean();

	public static boolean compareByType(List<Counter> counters, CommandType type)
	{
		Map<CommandType, Integer> outgoingMap = new HashMap<>();
		Map<CommandType, Integer> incomingMap = new HashMap<>();
		for (Counter counter : counters)
		{
			if (counter.getIsIncoming())
				incomingMap.put(counter.getCommand(), counter.getCount());
			else
				outgoingMap.put(counter.getCommand(), counter.getCount());
		}
		Integer outCount = outgoingMap.get(type);
		Integer inCount = null;
		switch (type)
		{
		case CONNECT:
			inCount = incomingMap.get(CommandType.CONNACK);
			break;
		case PINGREQ:
			inCount = incomingMap.get(CommandType.PINGRESP);
			break;
		case PUBREL:
			inCount = incomingMap.get(CommandType.PUBCOMP);
			break;
		case SUBSCRIBE:
			inCount = incomingMap.get(CommandType.SUBACK);
			break;
		case UNSUBSCRIBE:
			inCount = incomingMap.get(CommandType.UNSUBACK);
			break;
		default:
			break;
		}
		return outCount == inCount;
	}

	public static boolean comparePublish(List<Counter> counters, Integer qos)
	{
		if (qos == 1 || qos == 2)
		{
			Map<CommandType, Integer> outgoingMap = new HashMap<>();
			Map<CommandType, Integer> incomingMap = new HashMap<>();
			for (Counter counter : counters)
			{
				if (counter.getIsIncoming())
					incomingMap.put(counter.getCommand(), counter.getCount());
				else
					outgoingMap.put(counter.getCommand(), counter.getCount());
			}
			Integer outCount = outgoingMap.get(CommandType.PUBLISH);
			Integer inCount = null;
			if (qos == 1)
				inCount = incomingMap.get(CommandType.PUBACK);
			else
				inCount = incomingMap.get(CommandType.PUBREC);
			return outCount == inCount;
		}
		else
			return true;
	}
}
