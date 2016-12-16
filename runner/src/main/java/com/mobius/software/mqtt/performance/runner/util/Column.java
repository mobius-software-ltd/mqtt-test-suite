package com.mobius.software.mqtt.performance.runner.util;

public class Column
{
	private final String value;
	private final Alignment alignment;

	public Column(String value, Alignment alignment)
	{
		this.value = value;
		this.alignment = alignment;
	}

	public String getValue()
	{
		return value;
	}

	public Alignment getAlignment()
	{
		return alignment;
	}
}
