package com.mobius.software.mqtt.client.util;

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
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mobius.software.mqtt.client.api.data.Command;
import com.mobius.software.mqtt.client.api.data.CommandType;
import com.mobius.software.mqtt.client.api.data.Property;
import com.mobius.software.mqtt.client.api.data.PropertyType;
import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.avps.QoS;
import com.mobius.software.mqtt.parser.avps.Text;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Connect;
import com.mobius.software.mqtt.parser.header.impl.Publish;
import com.mobius.software.mqtt.parser.header.impl.Subscribe;
import com.mobius.software.mqtt.parser.header.impl.Unsubscribe;

public class CommandParser
{
	public static boolean validate(Command command)
	{
		if (command == null || command.getType() == null)
			return false;

		Map<PropertyType, String> propertyMap = new HashMap<>();
		if (command.getType() != CommandType.DISCONNECT)
		{
			for (Property property : command.getCommandProperties())
				propertyMap.put(property.getType(), property.getValue());
		}
		try
		{
			switch (command.getType())
			{
			case CONNECT:

				String username = propertyMap.remove(PropertyType.USERNAME);
				if (username == null || username.isEmpty())
					return false;
				String password = propertyMap.remove(PropertyType.PASSWORD);
				if (password == null || password.isEmpty())
					return false;
				String cleanSession = propertyMap.remove(PropertyType.CLEAN_SESSION);
				if (cleanSession == null || !(cleanSession.equals("true") || cleanSession.equals("false")))
					return false;
				String keepalive = propertyMap.remove(PropertyType.KEEPALIVE);
				if (keepalive == null || Integer.parseInt(keepalive) < 0)
					return false;
				return propertyMap.isEmpty();

			case DISCONNECT:
				return command.getCommandProperties() == null || command.getCommandProperties().isEmpty();

			case PUBLISH:
				String publishTopic = propertyMap.remove(PropertyType.TOPIC);
				if (publishTopic == null || publishTopic.isEmpty())
					return false;
				String publishQos = propertyMap.remove(PropertyType.QOS);
				if (publishQos == null || QoS.valueOf(Integer.parseInt(publishQos)) == null)
					return false;
				String retain = propertyMap.remove(PropertyType.RETAIN);
				if (retain != null && !(retain.equals("true") || retain.equals("false")))
					return false;
				String duplicate = propertyMap.remove(PropertyType.DUPLICATE);
				if (duplicate != null && !(duplicate.equals("true") || duplicate.equals("false")))
					return false;
				String count = propertyMap.remove(PropertyType.COUNT);
				if (count == null || Integer.parseInt(count) < 0)
					return false;
				String resendTime = propertyMap.remove(PropertyType.RESEND_TIME);
				if (resendTime == null || Integer.parseInt(resendTime) < 0)
					return false;
				return propertyMap.isEmpty();

			case SUBSCRIBE:
				String subscribeTopic = propertyMap.remove(PropertyType.TOPIC);
				if (subscribeTopic == null || subscribeTopic.isEmpty())
					return false;
				String subscribeQos = propertyMap.remove(PropertyType.QOS);
				if (subscribeQos == null || QoS.valueOf(Integer.parseInt(subscribeQos)) == null)
					return false;
				return propertyMap.isEmpty();

			case UNSUBSCRIBE:
				String unsubscribeTopic = propertyMap.remove(PropertyType.TOPIC);
				if (unsubscribeTopic == null || unsubscribeTopic.isEmpty())
					return false;
				return propertyMap.isEmpty();

			default:
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static MQMessage toMessage(Command command, String identRegex, Integer startIdent, String serverHostname)
	{
		Map<PropertyType, String> propertyMap = new HashMap<>();
		if (command.getType() != CommandType.DISCONNECT)
			for (Property property : command.getCommandProperties())
				propertyMap.put(property.getType(), property.getValue());

		switch (command.getType())
		{
		case CONNECT:
			String username = propertyMap.get(PropertyType.USERNAME);
			String password = propertyMap.get(PropertyType.PASSWORD);
			String identifier = IdentifierParser.parseIdentifier(identRegex, username, serverHostname, startIdent);
			Boolean cleanSession = Boolean.parseBoolean(propertyMap.get(PropertyType.CLEAN_SESSION));
			Integer keepalive = Integer.parseInt(propertyMap.get(PropertyType.KEEPALIVE));
			return new Connect(username, password, identifier, cleanSession, keepalive, null);

		case DISCONNECT:
			return MQParser.DISCONNECT_MESSAGE;

		case PUBLISH:
			Text publishTopicName = new Text(propertyMap.get(PropertyType.TOPIC));
			QoS publishQos = QoS.valueOf(Integer.valueOf(propertyMap.get(PropertyType.QOS)));
			Topic publishTopic = new Topic(publishTopicName, publishQos);
			Boolean retain = Boolean.parseBoolean(propertyMap.get(PropertyType.RETAIN));
			Boolean dup = Boolean.parseBoolean(propertyMap.get(PropertyType.DUPLICATE));
			byte[] content = MessageGenerator.generateContent();
			return new Publish(publishTopic, content, retain, dup);

		case SUBSCRIBE:
			Text subscribeTopicName = new Text(propertyMap.get(PropertyType.TOPIC));
			QoS subscribeQos = QoS.valueOf(Integer.valueOf(propertyMap.get(PropertyType.QOS)));
			Topic subscribeTopic = new Topic(subscribeTopicName, subscribeQos);
			return new Subscribe(new Topic[]
			{ subscribeTopic });

		case UNSUBSCRIBE:
			Text unsubscribeTopic = new Text(propertyMap.get(PropertyType.TOPIC));
			return new Unsubscribe(new Text[]
			{ unsubscribeTopic });

		default:
			return null;
		}
	}

	public static ConcurrentLinkedQueue<Command> retrieveCommands(List<Command> commands)
	{
		ConcurrentLinkedQueue<Command> queue = new ConcurrentLinkedQueue<>();
		for (Command command : commands)
		{
			if (command.getType() == CommandType.PUBLISH)
			{
				Integer count = retrieveIntegerProperty(command, PropertyType.COUNT);
				Integer resendTime = retrieveIntegerProperty(command, PropertyType.RESEND_TIME);
				Long currSendTime = command.getSendTime();
				for (int i = 1; i <= count; i++, currSendTime += resendTime)
				{
					Command publish = new Command(command.getType(), currSendTime, command.getCommandProperties());
					queue.offer(publish);
				}
			}
			else
				queue.offer(command);
		}
		return queue;
	}

	private static Integer retrieveIntegerProperty(Command command, PropertyType type)
	{
		Integer value = null;
		for (Property property : command.getCommandProperties())
		{
			if (property.getType() == type)
			{
				value = Integer.parseInt(property.getValue());
				break;
			}
		}
		return value;
	}
}
