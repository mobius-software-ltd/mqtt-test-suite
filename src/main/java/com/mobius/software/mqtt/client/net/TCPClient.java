package com.mobius.software.mqtt.client.net;

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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.client.MQTTException;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class TCPClient implements NetworkListener
{
	private static final Log logger = LogFactory.getLog(TCPClient.class);

	private Bootstrap bootstrap = new Bootstrap();
	private EpollEventLoopGroup loopGroup = new EpollEventLoopGroup(16);
	private ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners = new ConcurrentHashMap<SocketAddress, ConnectionListener>();
	private ConcurrentHashMap<SocketAddress, Channel> clientChannels = new ConcurrentHashMap<SocketAddress, Channel>();
	private AtomicBoolean status = new AtomicBoolean(false);
	private CountDownLatch latch = new CountDownLatch(1);

	public ChannelFuture connect(SocketAddress serverAddress)
	{
		if (status.compareAndSet(false, true))
		{
			bootstrap.group(loopGroup);
			bootstrap.channel(EpollSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

			bootstrap.handler(new ChannelInitializer<SocketChannel>()
			{
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception
				{
					socketChannel.pipeline().addLast(new Decoder());
					socketChannel.pipeline().addLast(new Encoder());
					socketChannel.pipeline().addLast("handler", new Handler(clientListeners));
					socketChannel.pipeline().addLast(new ExceptionHandler());
				}
			});
			bootstrap.remoteAddress(serverAddress);
			latch.countDown();
		}
		try
		{
			latch.await();
			ChannelFuture connectFuture = bootstrap.connect();
			return connectFuture;
		}
		catch (InterruptedException e)
		{
			throw new MQTTException(MessageType.CONNECT, "An error occured while establishing network connection: " + e.getMessage());
		}
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
		if (loopGroup != null)
		{
			Future<?> future = loopGroup.shutdownGracefully();
			try
			{
				future.await();
			}
			catch (InterruptedException e)
			{
				logger.error("An error occured while performing shutdown: interrupted loopGroup shutdown");
			}
		}
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
