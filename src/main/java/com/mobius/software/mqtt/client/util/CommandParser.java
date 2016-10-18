package com.mobius.software.mqtt.client.util;

import java.util.HashMap;
import java.util.Map;

import com.mobius.software.mqtt.client.api.data.Command;
import com.mobius.software.mqtt.client.api.data.CommandType;
import com.mobius.software.mqtt.client.api.data.Property;
import com.mobius.software.mqtt.client.api.data.PropertyType;
import com.mobius.software.mqtt.parser.QoS;
import com.mobius.software.mqtt.parser.Text;
import com.mobius.software.mqtt.parser.Topic;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Connect;
import com.mobius.software.mqtt.parser.header.impl.Disconnect;
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
			for (Property property : command.getCommandProperties())
				propertyMap.put(property.getType(), property.getValue());
		try
		{
			switch (command.getType())
			{
			case CONNECT:
				String identRegex = propertyMap.remove(PropertyType.IDENT_REGEX);
				if (identRegex == null || !IdentifierParser.validate(identRegex))
					return false;
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

	public static MQMessage toMessage(Command command)
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
			String identifier = IdentifierParser.parseIdentifier(propertyMap.get(PropertyType.IDENT_REGEX), username);
			Boolean cleanSession = Boolean.parseBoolean(propertyMap.get(PropertyType.CLEAN_SESSION));
			Integer keepalive = Integer.parseInt(propertyMap.get(PropertyType.KEEPALIVE));
			return new Connect(username, password, identifier, cleanSession, keepalive, null);
		case DISCONNECT:
			return new Disconnect();
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
}
