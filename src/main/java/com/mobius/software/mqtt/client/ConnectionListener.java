package com.mobius.software.mqtt.client;

import java.net.SocketAddress;

import com.mobius.software.mqtt.parser.header.api.MQMessage;

public interface ConnectionListener
{
	void packetReceived(SocketAddress address, MQMessage header);

	void connectionDown(SocketAddress address);
}
