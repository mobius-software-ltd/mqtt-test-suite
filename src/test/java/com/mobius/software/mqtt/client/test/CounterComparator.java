package com.mobius.software.mqtt.client.test;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mobius.software.mqtt.client.api.data.CommandType;
import com.mobius.software.mqtt.client.api.data.Counter;

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
