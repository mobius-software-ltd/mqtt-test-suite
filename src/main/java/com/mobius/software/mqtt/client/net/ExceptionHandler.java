package com.mobius.software.mqtt.client.net;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

import org.apache.log4j.Logger;

public class ExceptionHandler extends ChannelDuplexHandler
{
	private static Logger logger = Logger.getLogger(ExceptionHandler.class);
	private static final String separator = ",";

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		SocketAddress address = ctx.channel().remoteAddress();
		if (ctx.channel().isOpen())
			ctx.channel().close();

		StringBuilder sb = new StringBuilder();
		sb.append(address).append(separator);
		sb.append(cause.getClass().getName().substring(cause.getClass().getName().lastIndexOf(".") + 1)).append(separator);
		sb.append(cause.getMessage().substring(cause.getMessage().lastIndexOf(".") + 1));
		logger.error(sb.toString());
	}

	@Override
	public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise)
	{
		ctx.connect(remoteAddress, localAddress, promise.addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future)
			{
				if (!future.isSuccess())
					logger.error("an error occured while tcp connect," + future.channel().localAddress());
			}
		}));
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
	{
		ctx.write(msg, promise.addListener(new ChannelFutureListener()
		{
			@Override
			public void operationComplete(ChannelFuture future)
			{
				if (!future.isSuccess())
					logger.error("channel write operation failed");
			}
		}));
	}
}