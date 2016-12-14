package com.mobius.software.mqtt.performance.commons.data;

import java.util.HashMap;
import java.util.Map;

public enum Direction
{
	INCOMING(0), OUTGOING(1);

	private static final Map<Integer, Direction> intToTypeMap = new HashMap<Integer, Direction>();
	static
	{
		for (Direction type : Direction.values())
			intToTypeMap.put(type.value, type);
	}

	int value;

	Direction(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	public static Direction fromValue(int value)
	{
		return intToTypeMap.get(value);
	}
}
