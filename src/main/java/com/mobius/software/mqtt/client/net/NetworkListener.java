package com.mobius.software.mqtt.client.net;

import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;

import com.mobius.software.mqtt.client.ConnectionListener;
import com.mobius.software.mqtt.parser.header.api.MQMessage;

public interface NetworkListener
{
	ChannelFuture connect(SocketAddress serverAddress);

	SocketAddress finishConnection(ChannelFuture future,ConnectionListener listener);
	
	void send(SocketAddress address, MQMessage message);

	void close(SocketAddress address);
}