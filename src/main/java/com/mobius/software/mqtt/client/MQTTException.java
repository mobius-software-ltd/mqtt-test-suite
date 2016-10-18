package com.mobius.software.mqtt.client;

import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class MQTTException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private MessageType type;

	public MQTTException(MessageType type, String message)
	{
		super(message);
		this.type = type;
	}

	public MessageType getType()
	{
		return type;
	}
}
