package com.mobius.software.mqtt.client.api.data;

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
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mobius.software.mqtt.parser.header.impl.MessageType;

public enum CommandType
{
	CONNECT(1), SUBSCRIBE(2), PUBLISH(3), UNSUBSCRIBE(4), DISCONNECT(5), CONNACK(6), SUBACK(7), UNSUBACK(8), PUBACK(9), PUBREC(10), PUBREL(11), PUBCOMP(12), PINGREQ(13), PINGRESP(14);

	private static final Map<Integer, CommandType> intToTypeMap = new HashMap<Integer, CommandType>();
	private static final Map<String, CommandType> strToTypeMap = new HashMap<String, CommandType>();

	static
	{
		for (CommandType type : CommandType.values())
		{
			intToTypeMap.put(type.value, type);
			strToTypeMap.put(type.name(), type);
		}
	}

	public static CommandType fromMessageType(MessageType type)
	{
		switch (type)
		{
		case CONNACK:
			return CONNACK;
		case CONNECT:
			return CONNECT;
		case DISCONNECT:
			return DISCONNECT;
		case PINGREQ:
			return PINGREQ;
		case PINGRESP:
			return PINGRESP;
		case PUBACK:
			return PUBACK;
		case PUBCOMP:
			return PUBCOMP;
		case PUBLISH:
			return PUBLISH;
		case PUBREC:
			return PUBREC;
		case PUBREL:
			return PUBREL;
		case SUBACK:
			return SUBACK;
		case SUBSCRIBE:
			return SUBSCRIBE;
		case UNSUBACK:
			return UNSUBACK;
		case UNSUBSCRIBE:
			return UNSUBSCRIBE;
		default:
			return null;
		}
	}

	public static CommandType fromInt(int i)
	{
		return intToTypeMap.get(Integer.valueOf(i));
	}

	int value;

	private CommandType(int value)
	{
		this.value = value;
	}

	@JsonValue
	public int getValue()
	{
		return value;
	}

	@JsonCreator
	public static CommandType forValue(String value)
	{
		Integer intValue = null;
		try
		{
			intValue = Integer.parseInt(value);
		}
		catch (Exception ex)
		{

		}

		if (intValue != null)
			return intToTypeMap.get(intValue);
		else
			return strToTypeMap.get(value);
	}
}
