package com.mobius.software.mqtt.performance.controller.net;

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

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExceptionHandler extends ChannelDuplexHandler
{
	private static final Log logger = LogFactory.getLog(ExceptionHandler.class);
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
		cause.printStackTrace();
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