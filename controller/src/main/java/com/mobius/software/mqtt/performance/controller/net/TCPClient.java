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

package com.mobius.software.mqtt.performance.controller.net;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.performance.controller.client.MQTTException;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

public class TCPClient implements NetworkHandler
{
	private static final Log logger = LogFactory.getLog(TCPClient.class);

	private ConcurrentHashMap<SocketAddress, ClientBootstrap> bootstraps = new ConcurrentHashMap<>();
	private ConcurrentHashMap<SocketAddress, Channel> clientChannels = new ConcurrentHashMap<>();
	private ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners = new ConcurrentHashMap<>();

	public void init(SocketAddress serverAddress)
	{
		try
		{
			ClientBootstrap bootstrap = bootstraps.get(serverAddress);
			if (bootstrap == null)
			{
				bootstrap = new ClientBootstrap(clientListeners);
				ClientBootstrap oldBootstrap = bootstraps.putIfAbsent(serverAddress, bootstrap);
				if (oldBootstrap != null)
					bootstrap = oldBootstrap;
				else
					bootstrap.init(serverAddress);
			}
		}
		catch (InterruptedException e)
		{
			throw new MQTTException(MessageType.CONNECT, "An error occured while establishing network connection: " + e.getMessage());
		}
	}

	public ChannelFuture connect(SocketAddress serverAddress)
	{
		ClientBootstrap bootstrap = bootstraps.get(serverAddress);
		if (bootstrap != null)
			return bootstrap.createConnection();

		return null;
	}

	public SocketAddress finishConnection(ChannelFuture future, ConnectionListener listener)
	{
		if (future != null)
		{
			Channel channel = future.channel();
			SocketAddress localAddress = channel.localAddress();
			clientListeners.put(localAddress, listener);
			clientChannels.put(localAddress, channel);
			return localAddress;
		}

		return null;
	}

	public void shutdown() throws InterruptedException
	{

		Iterator<Entry<SocketAddress, Channel>> iterator = clientChannels.entrySet().iterator();
		while (iterator.hasNext())
		{
			try
			{
				iterator.next().getValue().close();
				iterator.remove();
			}
			catch (Exception e)
			{
				logger.error("An error occured while performing shutdown: client channel close failed - " + e.getMessage());
			}
		}
	}

	@Override
	public void send(SocketAddress address, MQMessage message)
	{
		Channel channel = clientChannels.get(address);
		if (channel != null && channel.isOpen())
			channel.writeAndFlush(message);
	}

	@Override
	public void close(SocketAddress address)
	{
		Channel channel = clientChannels.remove(address);
		if (channel != null)
		{
			try
			{
				channel.pipeline().remove("handler");
			}
			catch (NoSuchElementException ex)
			{

			}
			channel.close();
		}
	}
}