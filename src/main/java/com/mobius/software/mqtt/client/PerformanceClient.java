package com.mobius.software.mqtt.client;

import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.mobius.software.mqtt.client.api.data.Command;
import com.mobius.software.mqtt.client.api.data.ConnectionContext;
import com.mobius.software.mqtt.client.controller.PeriodicQueuedTasks;
import com.mobius.software.mqtt.client.controller.ScenarioOrchestrator;
import com.mobius.software.mqtt.client.controller.task.MessageResendTimer;
import com.mobius.software.mqtt.client.controller.task.Timer;
import com.mobius.software.mqtt.client.net.NetworkListener;
import com.mobius.software.mqtt.client.util.CommandParser;
import com.mobius.software.mqtt.parser.QoS;
import com.mobius.software.mqtt.parser.Text;
import com.mobius.software.mqtt.parser.Topic;
import com.mobius.software.mqtt.parser.Will;
import com.mobius.software.mqtt.parser.header.api.MQDevice;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.*;

public class PerformanceClient implements MQDevice, ConnectionListener, Timer
{
	private AtomicBoolean status = new AtomicBoolean();
	private ConnectionContext ctx;
	private NetworkListener listener;

	private ConcurrentLinkedQueue<Command> commands = new ConcurrentLinkedQueue<>();
	private AtomicReference<Command> pendingCommand = new AtomicReference<>();
	private AtomicReference<ChannelFuture> channelHandler = new AtomicReference<ChannelFuture>();
	private AtomicLong currDelay = new AtomicLong();

	private ConcurrentHashMap<Text, QoS> subscriptions = new ConcurrentHashMap<>();
	private ConcurrentSkipListSet<Integer> pendingIncomingIdentifiers = new ConcurrentSkipListSet<>();
	private TimersMap timers;
	private AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());
	private ScenarioOrchestrator orchestrator;
	private Integer initialDelay;

	private boolean continueOnError;
	private IdentityReport report = new IdentityReport();

	public PerformanceClient(PeriodicQueuedTasks<Timer> scheduler, NetworkListener listener, List<Command> commands, SocketAddress serverAddress, Long resendInterval, boolean continueOnError, ScenarioOrchestrator orchestrator, Integer initialDelay)
	{
		this.ctx = new ConnectionContext(serverAddress, resendInterval);
		this.listener = listener;
		this.commands.addAll(commands);
		this.timers = new TimersMap(ctx, scheduler, listener, report);
		this.continueOnError = continueOnError;
		this.orchestrator = orchestrator;
		this.initialDelay = initialDelay;
	}

	public Long getTimestamp()
	{
		return timestamp.get();
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
						ctx.updateLocalAddress(listener.finishConnection(future, this));
						status.set(true);						
					}
				}
				else
					channelHandler.compareAndSet(null, listener.connect(ctx.remoteAddress()));

				timestamp.set(System.currentTimeMillis() + initialDelay);
				return true;
			}
			else
			{				
				Command nextCommand = commands.poll();
				if (nextCommand != null)
				{
					if (!pendingCommand.compareAndSet(null, nextCommand))
					{
						report.reportError("PREVIOUS FAILED", pendingCommand.get().getType().toString());
						if (!continueOnError)
						{
							listener.close(ctx.localAddress());
							return false;
						}
					}

					MQMessage message = CommandParser.toMessage(nextCommand);
					switch (message.getType())
					{
					case DISCONNECT:
						timers.stopAllTimers();
						pendingCommand.set(null);
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
						report.setClientID(connect.getClientID());
						ctx.update(connect.isClean(), connect.getKeepAlive(), connect.getClientID());
						timers.storeConnect(message);
						break;
					default:
						break;
					}
					report.countOut(message.getType());
					listener.send(ctx.localAddress(), message);

					Command next = commands.peek();
					if (next != null)
						currDelay.set(next.getSendTime());
					else
						currDelay.set((System.currentTimeMillis() - timestamp.get()) + initialDelay);
				}
				else
				{
					listener.close(ctx.localAddress());
					orchestrator.notifyOnComplete();
				}

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
		return timestamp.get() + currDelay.get();
	}

	@Override
	public void processConnack(ConnackCode code, boolean sessionPresent)
	{
		Timer timer = timers.retrieveConnect();
		if (timer == null)
			throw new MQTTException(MessageType.CONNACK, "invalid packet identifier");
		timer.stop();

		if (ctx.getCleanSession() && sessionPresent)
			throw new MQTTException(MessageType.CONNACK, "invalid connack sessionPresent flag");

		switch (code)
		{
		case ACCEPTED:
			pendingCommand.set(null);
			timers.restartKeepalive();
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
			throw new MQTTException(MessageType.SUBACK, "invalid packet identifier");

		timers.restartKeepalive();

		Subscribe subscribe = (Subscribe) timer.retrieveMessage();
		Topic[] topics = subscribe.getTopics();
		if (topics.length != codes.size())
			throw new MQTTException(MessageType.SUBACK, "Invalid codes length: " + codes.size() + ", expected" + topics.length);

		for (int i = 0; i < topics.length; i++)
		{
			Topic topic = subscribe.getTopics()[i];
			SubackCode code = codes.get(i);
			if (code != SubackCode.FAILURE)
			{
				QoS allowedQos = QoS.valueOf(code.getNum());
				subscriptions.put(topic.getName(), allowedQos);
			}
		}
		pendingCommand.set(null);
	}

	@Override
	public void processUnsuback(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.UNSUBSCRIBE)
			throw new MQTTException(MessageType.UNSUBACK, "invalid packet identifier");

		timers.restartKeepalive();

		Unsubscribe unsub = (Unsubscribe) timer.retrieveMessage();
		subscriptions.keySet().removeAll(Arrays.asList(unsub.getTopics()));
		pendingCommand.set(null);
	}

	@Override
	public void processPublish(Integer packetID, Topic topic, byte[] content, boolean retain, boolean isDup)
	{
		if (isDup)
			report.countDuplicateIn();
		switch (topic.getQos())
		{
		case AT_LEAST_ONCE:
			listener.send(ctx.localAddress(), new Puback(packetID));
			pendingCommand.set(null);
			break;
		case EXACTLY_ONCE:
			pendingIncomingIdentifiers.add(packetID);
			listener.send(ctx.localAddress(), new Pubrec(packetID));
			pendingCommand.set(null);
			break;
		case AT_MOST_ONCE:
		default:
			break;
		}
	}

	@Override
	public void processPuback(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.PUBLISH)
			throw new MQTTException(MessageType.PUBACK, "invalid packet identifier");

		timers.restartKeepalive();
		timers.remove(packetID);
		pendingCommand.set(null);
	}

	@Override
	public void processPubrec(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.PUBLISH)
			throw new MQTTException(MessageType.PUBREC, "invalid packet identifier");

		Pubrel pubrel = new Pubrel(packetID);
		timers.store(pubrel);
		listener.send(ctx.localAddress(), pubrel);
		pendingCommand.set(null);
	}

	@Override
	public void processPubrel(Integer packetID)
	{
		Pubcomp pubcomp = new Pubcomp(packetID);
		listener.send(ctx.localAddress(), pubcomp);
		if (!pendingIncomingIdentifiers.remove(packetID))
			throw new MQTTException(MessageType.PUBREL, "Unrecognized identifier");
		pendingCommand.set(null);
	}

	@Override
	public void processPubcomp(Integer packetID)
	{
		MessageResendTimer timer = timers.remove(packetID);
		if (timer == null || timer.retrieveMessage().getType() != MessageType.PUBREL)
			throw new MQTTException(MessageType.PUBCOMP, "invalid packet identifier");

		pendingCommand.set(null);
	}

	@Override
	public void processPingresp()
	{
		timers.restartKeepalive();
	}

	@Override
	public void processConnect(boolean cleanSession, int keepalive, Will will)
	{
		throw new MQTTException(MessageType.CONNECT, "Received invalid message from broker");
	}

	@Override
	public void processSubscribe(Integer packetID, Topic[] topics)
	{
		throw new MQTTException(MessageType.SUBSCRIBE, "Received invalid message from broker");
	}

	@Override
	public void processUnsubscribe(Integer packetID, Text[] topics)
	{
		throw new MQTTException(MessageType.UNSUBSCRIBE, "Received invalid message from broker");
	}

	@Override
	public void processPingreq()
	{
		throw new MQTTException(MessageType.PINGREQ, "Received invalid message from broker");
	}

	@Override
	public void processDisconnect()
	{
		throw new MQTTException(MessageType.DISCONNECT, "Received invalid message from broker");
	}

	@Override
	public void connectionDown(SocketAddress address)
	{
		report.reportError("CONNECTION", "connection closed:" + address);
		timers.stopAllTimers();
		pendingIncomingIdentifiers.clear();
		if (ctx.getCleanSession() != null && ctx.getCleanSession())
			subscriptions.clear();
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
			report.reportError(e.getType().toString(), e.getMessage());
		}
	}

	@Override
	public void stop()
	{
		//
	}

	public IdentityReport getReport()
	{
		return report;
	}
}
