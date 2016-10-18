package com.mobius.software.mqtt.client.api.data;

public class Property
{
	private PropertyType type;
	private String value;

	public Property()
	{

	}

	public Property(PropertyType type, String value)
	{
		this.type = type;
		this.value = value;
	}

	public PropertyType getType()
	{
		return type;
	}

	public void setType(PropertyType type)
	{
		this.type = type;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}
}
