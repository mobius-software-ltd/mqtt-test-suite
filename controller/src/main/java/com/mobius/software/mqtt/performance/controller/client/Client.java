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

package com.mobius.software.mqtt.performance.controller.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.mobius.software.mqtt.parser.avps.ConnackCode;
import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.avps.SubackCode;
import com.mobius.software.mqtt.parser.avps.Text;
import com.mobius.software.mqtt.parser.avps.Topic;
import com.mobius.software.mqtt.parser.avps.Will;
import com.mobius.software.mqtt.parser.header.api.MQDevice;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.*;
import com.mobius.software.mqtt.performance.api.data.ErrorType;
import com.mobius.software.mqtt.performance.commons.data.Command;
import com.mobius.software.mqtt.performance.commons.util.CommandParser;
import com.mobius.software.mqtt.performance.controller.Orchestrator;
import com.mobius.software.mqtt.performance.controller.net.ConnectionListener;
import com.mobius.software.mqtt.performance.controller.net.NetworkHandler;
import com.mobius.software.mqtt.performance.controller.task.MessageResendTimer;
import com.mobius.software.mqtt.performance.controller.task.TimedTask;

public class Client implements MQDevice, ConnectionListener, TimedTask
{
	private String clientID;
	private AtomicBoolean status = new AtomicBoolean();
	private ConnectionContext ctx;
	private NetworkHandler listener;

	private ConcurrentLinkedQueue<Command> commands = new ConcurrentLinkedQueue<>();
	private AtomicReference<Command> pendingCommand = new AtomicReference<>();
	private AtomicReference<ChannelFuture> channelHandler = new AtomicReference<ChannelFuture>();

	private TimersMap timers;
	private AtomicLong timestamp;
	private Orchestrator orchestrator;

	private AtomicInteger failedCommands = new AtomicInteger(0);
	private IdentityReport report;

	public Client(String clientID, Orchestrator orchestrator, NetworkHandler listener, ConcurrentLinkedQueue<Command> commands)
	{
		this.clientID = clientID;
		this.ctx = new ConnectionContext(orchestrator.getProperties().getServerAddress(), orchestrator.getProperties().getResendInterval());
		this.listener = listener;
		this.commands = commands;
		this.report = new IdentityReport(clientID);
		this.timers = new TimersMap(ctx, orchestrator.getScheduler(), listener, report);
		this.orchestrator = orchestrator;
		this.timestamp = new AtomicLong(System.currentTimeMillis() + orchestrator.getProperties().getInitialDelay() + orchestrator.getProperties().getScenarioDelay());
	}

	@Override
	public Boolean execute()
	{
		try
		{
			if (!status.get())
			{
				Boolean previouslyNull = (channelHandler.get() == null);
				if (!previouslyNull)
				{
					ChannelFuture future = channelHandler.get();
					if (future.isDone())
					{
						if (future.isSuccess())
						{
							ctx.updateLocalAddress(listener.finishConnection(future, this));
							status.set(true);
						}
						else
							report.reportError(ErrorType.CONNECTION_LOST, "failed to establish TCP connection");
					}
				}
				else
					channelHandler.compareAndSet(null, listener.connect(ctx.remoteAddress()));

				timestamp.set(System.currentTimeMillis() + orchestrator.getProperties().getInitialDelay());
				return true;
			}
			else
			{
				Long currTimestamp = timestamp.get();
				Command nextCommand = null;
				boolean firstIteration = true;
				do
				{
					nextCommand = commands.poll();
					if (nextCommand != null)
					{
						if (firstIteration)
						{
							Command previous = pendingCommand.getAndSet(nextCommand);
							if (previous != null && nextCommand.getSendTime() > 0)
							{
								report.reportError(ErrorType.PREVIOUS_COMMAND_FAILED, previous.getType().toString());
								failedCommands.incrementAndGet();
								if (!orchestrator.getProperties().isContinueOnError())
								{
									listener.close(ctx.localAddress());
									return false;
								}
							}
						}

						MQMessage message = CommandParser.toMessage(nextCommand, clientID);
						Boolean closeChannel = false;
						switch (message.getType())
						{
						case DISCONNECT:
							timers.stopAllTimers();
							pendingCommand.set(null);
							closeChannel = true;
							break;
						case PUBLISH:
							Publish pub = (Publish) message;
							switch (pub.getTopic().getQos())
							{
							case AT_MOST_ONCE:
								pendingCommand.set(null);
								break;
							case AT_LEAST_ONCE:
							case EXACTLY_ONCE:
								timers.store(pub);
								break;
							default:
								break;
							}
							break;
						case SUBSCRIBE:
						case UNSUBSCRIBE:
							timers.store(message);
							break;
						case CONNECT:
							Connect connect = (Connect) message;
							ctx.update(connect.isClean(), connect.getKeepAlive(), connect.getClientID());
							timers.storeConnect(message);
							break;
						default:
							break;
						}
						report.countOut(message.getType());
						listener.send(ctx.localAddress(), message);

						if (closeChannel)
						{
							status.set(false);
							channelHandler.set(null);
							listener.close(ctx.localAddress());
						}
						Command next = commands.peek();
						if (next != null)
							timestamp.addAndGet(next.getSendTime());
						else
							timestamp.set(System.currentTimeMillis() + orchestrator.getProperties().getInitialDelay());

						firstIteration = false;
					}
					else
						orchestrator.notifyOnComplete();
				}
				while (nextCommand != null && currTimestamp == timestamp.get());

				return nextCommand != null;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Long getRealTimestamp()
	{
		return timestamp.get();
	}

	public Command checkNext()
	{
		return commands.peek();
	}

	@Override
	public void processConnack(ConnackCode code, boolean sessionPresent)
	{
		orchestrator.notifyOnStart();
		TimedTask timer = timers.retrieveConnect();
		if (timer != null)
			timer.stop();

		if (ctx.getCleanSession() && sessionPresent)
			throw new MQTTException(MessageType.CONNACK, "invalid connack sessionPresent flag");

		switch (code)
		{
		case ACCEPTED:
			pendingCommand.set(null);
			timers.restartPing();
			break;
		case BAD_USER_OR_PASS:
		case IDENTIFIER_REJECTED:
		case NOT_AUTHORIZED:
		case SERVER_UNUVALIABLE:
		case UNACCEPTABLE_PROTOCOL_VERSION:
		default:
			listener.close(ctx.localAddress());
			throw new MQTTException(MessageType.CONNACK, "server rejected connection: " + code);
		}
	}

	@Override
	public void processSuback(Integer packetID, List<SubackCode> codes)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.SUBSCRIBE)
			report.reportError(ErrorType.SUBACK, "invalid packet identifier");
		timers.restartPing();

		Subscribe subscribe = (Subscribe) timer.retrieveMessage();
		Topic[] topics = subscribe.getTopics();
		if (topics.length != codes.size())
			report.reportError(ErrorType.SUBACK, "Invalid codes length: " + codes.size() + ", expected" + topics.length);

		pendingCommand.set(null);
	}

	@Override
	public void processUnsuback(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.UNSUBSCRIBE)
			report.reportError(ErrorType.UNSUBACK, "invalid packet identifier");

		timers.restartPing();
		pendingCommand.set(null);
	}

	@Override
	public void processPublish(Integer packetID, Topic topic, ByteBuf content, boolean retain, boolean isDup)
	{
		if (isDup)
		{
			report.countDuplicateIn();
			report.reportError(ErrorType.DUPLICATE, "PUBLISH received");
		}
		MQMessage response = null;
		switch (topic.getQos())
		{
		case AT_LEAST_ONCE:
			response = new Puback(packetID);
			break;
		case EXACTLY_ONCE:
			response = new Pubrec(packetID);
			break;
		case AT_MOST_ONCE:
		default:
			break;
		}

		if (response != null)
		{
			listener.send(ctx.localAddress(), response);
			report.countOut(response.getType());
			pendingCommand.set(null);
		}
	}

	@Override
	public void processPuback(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.PUBLISH)
			report.reportError(ErrorType.PUBACK, "invalid packet identifier");

		timers.restartPing();
		pendingCommand.set(null);
	}

	@Override
	public void processPubrec(Integer packetID)
	{
		Pubrel pubrel = new Pubrel(packetID);
		MessageResendTimer oldTimer = timers.store(pubrel);
		if (oldTimer == null || oldTimer.retrieveMessage().getType() != MessageType.PUBLISH)
			report.reportError(ErrorType.PUBREC, "invalid packet identifier");
		listener.send(ctx.localAddress(), pubrel);
		report.countOut(pubrel.getType());
		pendingCommand.set(null);
	}

	@Override
	public void processPubrel(Integer packetID)
	{
		Pubcomp pubcomp = new Pubcomp(packetID);
		listener.send(ctx.localAddress(), pubcomp);
		report.countOut(pubcomp.getType());
		pendingCommand.set(null);
	}

	@Override
	public void processPubcomp(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.PUBREL)
			report.reportError(ErrorType.PUBCOMP, "invalid packet identifier");

		pendingCommand.set(null);
	}

	@Override
	public void processPingresp()
	{
		Command nextCommand = checkNext();
		if (nextCommand != null && getRealTimestamp() > (System.currentTimeMillis() + orchestrator.getProperties().getMinPingInterval() + ctx.getKeepalive() * 1000))
			timers.restartPing();
		else
			timers.stopPing();
	}

	@Override
	public void processConnect(boolean cleanSession, int keepalive, Will will)
	{
		report.reportError(ErrorType.CONNECT, "Received invalid message from broker");
	}

	@Override
	public void processSubscribe(Integer packetID, Topic[] topics)
	{
		report.reportError(ErrorType.SUBSCRIBE, "Received invalid message from broker");
	}

	@Override
	public void processUnsubscribe(Integer packetID, Text[] topics)
	{
		report.reportError(ErrorType.UNSUBSCRIBE, "Received invalid message from broker");
	}

	@Override
	public void processPingreq()
	{
		report.reportError(ErrorType.PINGREQ, "Received invalid message from broker");
	}

	@Override
	public void processDisconnect()
	{
		report.reportError(ErrorType.DISCONNECT, "Received invalid message from broker");
	}

	@Override
	public void connectionDown(SocketAddress address)
	{
		if (status.get())
		{
			report.reportError(ErrorType.CONNECTION_LOST, "connection closed:" + address);
			timers.stopAllTimers();
		}
	}

	@Override
	public void packetReceived(SocketAddress address, MQMessage message)
	{
		try
		{
			report.countIn(message.getType());
			message.processBy(this);
		}
		catch (MQTTException e)
		{
			report.reportError(ErrorType.forMessageType(e.getType()), e.getMessage());
		}
	}

	@Override
	public void stop()
	{
		if (ctx.localAddress() != null)
			listener.close(ctx.localAddress());

		timers.stopAllTimers();
	}

	public IdentityReport retrieveReport()
	{
		int commandsCount = commands.size() + failedCommands.get();
		if (pendingCommand.get() != null)
			commandsCount++;

		if (commandsCount > 0)
			System.out.println("clientID:" + clientID + ",address:" + ctx.localAddress() + "failed:" + failedCommands.get());

		report.setUnfinishedCommands(commandsCount);
		return report;
	}

	public String retrieveIdentifier()
	{
		return this.clientID;
	}
}
