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

import com.mobius.software.mqtt.parser.header.api.MQMessage;

import io.netty.channel.ChannelFuture;

public interface NetworkHandler
{
	ChannelFuture connect(SocketAddress serverAddress);

	SocketAddress finishConnection(ChannelFuture future, ConnectionListener listener);

	void send(SocketAddress address, MQMessage message);

	void close(SocketAddress address);

	void init(SocketAddress serverAddress);
	
	void shutdown() throws InterruptedException;
	
	void releaseLocalPort(SocketAddress serverAddress, int localPort);
}