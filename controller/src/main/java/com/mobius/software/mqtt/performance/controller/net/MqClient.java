package com.mobius.software.mqtt.performance.controller.net;

import java.net.SocketAddress;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.performance.controller.client.MQTTException;

import io.netty.channel.Channel;

public class MqClient extends AbstractClient
{
	public void init(SocketAddress serverAddress)
	{
		try
		{
			ClientBootstrap bootstrap = bootstraps.get(serverAddress);
			if (bootstrap == null)
			{
				bootstrap = new MqClientBootstrap(clientListeners);
				ClientBootstrap oldBootstrap = bootstraps.putIfAbsent(serverAddress, bootstrap);
				if (oldBootstrap != null)
					bootstrap = oldBootstrap;
				else
					bootstrap.init(serverAddress);
			}

			bootstrap.clearPorts();
		}
		catch (InterruptedException e)
		{
			throw new MQTTException(MessageType.CONNECT, "An error occured while establishing network connection: " + e.getMessage());
		}
	}

	@Override
	public void send(SocketAddress address, MQMessage message)
	{
		Channel channel = clientChannels.get(address);
		if (channel != null && channel.isOpen())
			channel.writeAndFlush(message);
	}
}
