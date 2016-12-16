package com.mobius.software.mqtt.performance.controller.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientBootstrap
{
	private static final Log logger = LogFactory.getLog(ClientBootstrap.class);

	private Bootstrap bootstrap = new Bootstrap();
	private AtomicBoolean pipelineInitialized = new AtomicBoolean(false);	
	private EpollEventLoopGroup loopGroup = new EpollEventLoopGroup(16);
	private ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners;

	public ClientBootstrap(ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		this.clientListeners = clientListeners;
	}

	public void init(SocketAddress serverAddress) throws InterruptedException
	{
		if (pipelineInitialized.compareAndSet(false, true))
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
		}	
	}

	public ChannelFuture createConnection()
	{
		return bootstrap.connect();
	}

	public void shutdown()
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
	}
}
