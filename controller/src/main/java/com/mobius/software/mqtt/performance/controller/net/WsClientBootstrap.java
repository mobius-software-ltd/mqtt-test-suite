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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

public class WsClientBootstrap extends ClientBootstrap
{
	private ExceptionHandler exceptionHandler = new ExceptionHandler();

	public WsClientBootstrap(ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		super(clientListeners);
	}

	@Override
	public void init(SocketAddress serverAddress) throws InterruptedException
	{
		this.serverAddress = serverAddress;
		if (pipelineInitialized.compareAndSet(false, true))
		{
			try
			{
				InetSocketAddress remote = (InetSocketAddress) serverAddress;
				URI uri = new URI("ws://" + remote.getHostString() + ":" + remote.getPort() + "/ws");

				bootstrap.group(loopGroup);
				bootstrap.channel(NioSocketChannel.class);
				bootstrap.option(ChannelOption.TCP_NODELAY, true);
				bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
				bootstrap.handler(new ChannelInitializer<SocketChannel>()
				{
					@Override
					protected void initChannel(SocketChannel socketChannel) throws Exception
					{
						socketChannel.pipeline().addLast("http-codec", new HttpClientCodec());
						socketChannel.pipeline().addLast("aggregator", new HttpObjectAggregator(65536));
						socketChannel.pipeline().addLast("handler", new WsClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, EmptyHttpHeaders.INSTANCE, 1280000, true, true), clientListeners));
						socketChannel.pipeline().addLast("compressor", WebSocketClientCompressionHandler.INSTANCE);
						socketChannel.pipeline().addLast("exceptionHandler", exceptionHandler);
					}
				});
				bootstrap.remoteAddress(serverAddress);
			}
			catch (URISyntaxException e)
			{
				throw new InterruptedException(e.getMessage());
			}
		}
	}

	@Override
	public ChannelFuture createConnection()
	{
		try
		{
			Channel channel = super.createConnection().sync().channel();
			return ((WsClientHandler) channel.pipeline().get("handler")).handshakeFuture();
		}
		catch (InterruptedException e)
		{
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
}
