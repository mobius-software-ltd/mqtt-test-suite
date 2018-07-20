package com.mobius.software.mqtt.performance.controller.net;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.mobius.software.mqtt.parser.MQJsonParser;
import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.header.api.MQMessage;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

public class WsClientHandler extends SimpleChannelInboundHandler<Object>
{
	private MQJsonParser parser = new MQJsonParser();

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;
	private ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners;

	public WsClientHandler(final WebSocketClientHandshaker handshaker, ConcurrentHashMap<SocketAddress, ConnectionListener> clientListeners)
	{
		this.handshaker = handshaker;
		this.clientListeners = clientListeners;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		final Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete())
		{
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			handshakeFuture.setSuccess();
			return;
		}

		if (msg instanceof FullHttpResponse)
		{
			final FullHttpResponse response = (FullHttpResponse) msg;
			throw new Exception("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
		}

		final WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof TextWebSocketFrame)
		{
			final TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
			MQMessage message = parser.messageObject(textFrame.text());
			MQParser.validate(message);

			SocketAddress address = ctx.channel().localAddress();
			ConnectionListener currListener = clientListeners.get(address);
			if (currListener != null)
				currListener.packetReceived(address, message);
		}
		else if (frame instanceof CloseWebSocketFrame)
			ch.close();
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

	public ChannelFuture handshakeFuture()
	{
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(final ChannelHandlerContext ctx) throws Exception
	{
		handshakeFuture = ctx.newPromise();
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) throws Exception
	{
		handshaker.handshake(ctx.channel());
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception
	{
		if (!handshakeFuture.isDone())
			handshakeFuture.setFailure(cause);

		throw new Exception(cause);
	}
}
