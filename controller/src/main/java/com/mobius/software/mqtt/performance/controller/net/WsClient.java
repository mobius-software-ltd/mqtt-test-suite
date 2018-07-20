package com.mobius.software.mqtt.performance.controller.net;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mobius.software.mqtt.parser.MQJsonParser;
import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.performance.controller.client.MQTTException;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class WsClient extends AbstractClient
{
	private MQJsonParser parser = new MQJsonParser();

	public void init(SocketAddress serverAddress)
	{
		try
		{
			ClientBootstrap bootstrap = bootstraps.get(serverAddress);
			if (bootstrap == null)
			{
				bootstrap = new WsClientBootstrap(clientListeners);
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
		{
			try
			{
				String json = parser.jsonString(message);
				channel.writeAndFlush(new TextWebSocketFrame(json));
			}
			catch (JsonProcessingException e)
			{
				logger.warn("error sending data to server", e);
			}
		}
	}

	@Override
	public void shutdown() throws InterruptedException
	{
		Iterator<Entry<SocketAddress, Channel>> iterator = clientChannels.entrySet().iterator();
		while (iterator.hasNext())
		{
			try
			{
				close(iterator.next().getKey());
				iterator.remove();
			}
			catch (Exception e)
			{
				logger.error("An error occured while performing shutdown: client channel close failed - " + e.getMessage());
			}
		}
	}

	@Override
	public void close(SocketAddress address)
	{
		Channel channel = clientChannels.remove(address);
		if (channel != null)
		{
			try
			{
				channel.writeAndFlush(new CloseWebSocketFrame());
				channel.closeFuture().sync();
			}
			catch (NoSuchElementException | InterruptedException ex)
			{
			}
		}
	}
}
