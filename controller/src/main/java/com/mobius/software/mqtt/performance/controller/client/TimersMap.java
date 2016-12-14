package com.mobius.software.mqtt.performance.controller.client;

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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import com.mobius.software.mqtt.parser.header.api.CountableMessage;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Pingreq;
import com.mobius.software.mqtt.parser.header.impl.Pubrel;
import com.mobius.software.mqtt.performance.api.data.ConnectionContext;
import com.mobius.software.mqtt.performance.api.data.IdentityReport;
import com.mobius.software.mqtt.performance.controller.PeriodicQueuedTasks;
import com.mobius.software.mqtt.performance.controller.net.NetworkListener;
import com.mobius.software.mqtt.performance.controller.task.MessageResendTimer;
import com.mobius.software.mqtt.performance.controller.task.Timer;

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
	private MessageResendTimer ping;

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

	public Timer store(Pubrel message)
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

		if (this.ping != null)
			ping.stop();

		for (Iterator<Map.Entry<Integer, MessageResendTimer>> it = timersMap.entrySet().iterator(); it.hasNext();)
		{
			Map.Entry<Integer, MessageResendTimer> entry = it.next();
			entry.getValue().stop();
		}

		timersMap.clear();
	}

	public void storeConnect(MQMessage message)
	{
		Timer timer = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		Timer oldTimer = connect.getAndSet(timer);
		if (oldTimer != null)
			oldTimer.stop();
	}

	public void restartPing()
	{
		if (ctx.getKeepalive() > 0)
		{
			if (this.ping == null)
				this.ping = new MessageResendTimer(ctx, scheduler, listener, new Pingreq(), (long) ctx.getKeepalive() * 1000, report);
			else
				this.ping.restart();

			scheduler.store(ping.getRealTimestamp(), ping);
		}
	}

	public void stopPing()
	{
		if (this.ping != null)
		{
			this.ping.stop();
		}
	}
}
