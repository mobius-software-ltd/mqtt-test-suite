package com.mobius.software.mqtt.client;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import com.mobius.software.mqtt.client.api.data.ConnectionContext;
import com.mobius.software.mqtt.client.controller.PeriodicQueuedTasks;
import com.mobius.software.mqtt.client.controller.task.MessageResendTimer;
import com.mobius.software.mqtt.client.controller.task.Timer;
import com.mobius.software.mqtt.client.net.NetworkListener;
import com.mobius.software.mqtt.parser.header.api.CountableMessage;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Pingreq;

public class TimersMap
{
	private static final int MAX_VALUE = 65535;
	private static final int FIRST_ID = 1;

	private IdentityReport report;
	private ConnectionContext ctx;
	private PeriodicQueuedTasks<Timer> scheduler;
	private NetworkListener listener;

	private ConcurrentSkipListMap<Integer, MessageResendTimer> timersMap = new ConcurrentSkipListMap<>();
	private AtomicReference<Timer> connect = new AtomicReference<>();
	private AtomicReference<Timer> ping = new AtomicReference<>();

	public TimersMap(ConnectionContext ctx, PeriodicQueuedTasks<Timer> scheduler, NetworkListener listener, IdentityReport report)
	{
		this.ctx = ctx;
		this.scheduler = scheduler;
		this.listener = listener;
		this.report = report;
	}

	public void store(MQMessage message)
	{
		MessageResendTimer timer = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		Integer packetID = (timersMap.isEmpty() || timersMap.lastKey() == MAX_VALUE) ? FIRST_ID : timersMap.lastKey();
		do
		{
			if (timersMap.size() == MAX_VALUE)
				throw new MQTTException(message.getType(), "outgoing identifier overflow");
			packetID++;
		}
		while (timersMap.putIfAbsent(packetID, timer) != null);
		CountableMessage countable = (CountableMessage) message;
		countable.setPacketID(packetID);
		scheduler.store(timer.getRealTimestamp(), timer);
	}

	public Timer store(CountableMessage message)
	{
		MessageResendTimer timer = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		Timer oldTimer = timersMap.put(message.getPacketID(), timer);
		if (oldTimer != null)
			oldTimer.stop();
		return timer;
	}

	public Timer retrieveConnect()
	{
		return connect.get();
	}

	public MessageResendTimer remove(Integer packetID)
	{
		MessageResendTimer timer = timersMap.remove(packetID);
		if (timer != null)
			timer.stop();
		return timer;
	}

	public void stopAllTimers()
	{
		if (this.connect.get() != null)
			connect.get().stop();

		if (this.ping.get() != null)
			ping.get().stop();

		for (Iterator<Map.Entry<Integer, MessageResendTimer>> it = timersMap.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Integer, MessageResendTimer> entry = it.next();
			entry.getValue().stop();
			it.remove();
		}
	}

	public void storeConnect(MQMessage message)
	{
		Timer timer = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		Timer oldTimer = connect.getAndSet(timer);
		if (oldTimer != null)
			oldTimer.stop();
	}

	public void restartKeepalive()
	{
		if (ctx.getKeepalive() > 0)
		{
			Timer ping = new MessageResendTimer(ctx, scheduler, listener, new Pingreq(), (long) ctx.getKeepalive() * 1000, report);
			Timer oldPing = this.ping.getAndSet(ping);
			if (oldPing != null)
				oldPing.stop();
			scheduler.store(ping.getRealTimestamp(), ping);
		}
	}
}
