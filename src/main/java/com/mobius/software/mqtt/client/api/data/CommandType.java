package com.mobius.software.mqtt.client.api.data;

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
