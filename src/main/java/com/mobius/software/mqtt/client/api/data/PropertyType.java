package com.mobius.software.mqtt.client.api.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PropertyType
{
	IDENT_REGEX(1), USERNAME(2), PASSWORD(3), CLEAN_SESSION(4), KEEPALIVE(5), TOPIC(6), QOS(7), RETAIN(8), DUPLICATE(9);

	private static final Map<Integer, PropertyType> intToTypeMap = new HashMap<Integer, PropertyType>();
	private static final Map<String, PropertyType> strToTypeMap = new HashMap<String, PropertyType>();

	static
	{
		for (PropertyType type : PropertyType.values())
		{
			intToTypeMap.put(type.value, type);
			strToTypeMap.put(type.name(), type);
		}
	}

	public static PropertyType fromInt(int i)
	{
		return intToTypeMap.get(Integer.valueOf(i));
	}

	int value;

	private PropertyType(int value)
	{
		this.value = value;
	}

	@JsonValue
	public int getValue()
	{
		return value;
	}

	@JsonCreator
	public static PropertyType forValue(String value)
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
