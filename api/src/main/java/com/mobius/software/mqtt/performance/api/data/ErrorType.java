package com.mobius.software.mqtt.performance.api.data;

import java.util.HashMap;
import java.util.Map;

import com.mobius.software.mqtt.parser.avps.MessageType;

public enum ErrorType
{
	CONNECT(0), CONNACK(1), SUBSCRIBE(2), SUBACK(3), UNSUBSCRIBE(4), UNSUBACK(5), PUBLISH(6), PUBACK(7), PUBREC(8), PUBREL(9), PUBCOMP(10), PINGREQ(11), PINGRESP(12), DISCONNECT(13), CONNECTION_LOST(14), PREVIOUS_COMMAND_FAILED(15);

	private static final Map<Integer, ErrorType> intToTypeMap = new HashMap<Integer, ErrorType>();
	private static final Map<String, ErrorType> strToTypeMap = new HashMap<String, ErrorType>();

	static
	{
		for (ErrorType type : ErrorType.values())
		{
			intToTypeMap.put(type.value, type);
			strToTypeMap.put(type.name(), type);
		}
	}

	public static ErrorType fromInt(int i)
	{
		return intToTypeMap.get(Integer.valueOf(i));
	}

	int value;

	private ErrorType(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	public static ErrorType forValue(String value)
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

	public static ErrorType forMessageType(MessageType messageType)
	{
		return strToTypeMap.get(messageType.toString());
	}
}
