package com.mobius.software.mqtt.client.net;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.mobius.software.mqtt.client.ConnectionListener;
import com.mobius.software.mqtt.parser.header.api.MQMessage;

public class Handler extends SimpleChannelInboundHandler<MQMessage>
{
	private ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners;

	public Handler(ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		this.clientListeners = clientListeners;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MQMessage message) throws Exception
	{
		SocketAddress address = ctx.channel().localAddress();
		ConnectionListener currListener = clientListeners.get(address);
		if (currListener != null)
			currListener.packetReceived(address, message);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception
	{
		SocketAddress address = ctx.channel().localAddress();
		ConnectionListener currListener = clientListeners.remove(address);
		if (currListener != null)
			currListener.connectionDown(address);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx)
	{
		ctx.flush();
	}
}
