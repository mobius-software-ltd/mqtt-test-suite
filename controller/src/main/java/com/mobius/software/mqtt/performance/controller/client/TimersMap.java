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

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.parser.MQParser;
import com.mobius.software.mqtt.parser.header.api.CountableMessage;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Pubrel;
import com.mobius.software.mqtt.performance.controller.PeriodicQueuedTasks;
import com.mobius.software.mqtt.performance.controller.net.NetworkHandler;
import com.mobius.software.mqtt.performance.controller.task.MessageResendTimer;
import com.mobius.software.mqtt.performance.controller.task.TimedTask;

public class TimersMap
{
	private static final int MAX_VALUE = 65535;
	private static final int FIRST_ID = 1;

	private IdentityReport report;
	private ConnectionContext ctx;
	private PeriodicQueuedTasks<TimedTask> scheduler;
	private NetworkHandler listener;

	private AtomicInteger currID = new AtomicInteger(0);
	private ConcurrentHashMap<Integer, MessageResendTimer> timersMap = new ConcurrentHashMap<>();
	private MessageResendTimer connect;
	private MessageResendTimer ping;

	public TimersMap(ConnectionContext ctx, PeriodicQueuedTasks<TimedTask> scheduler, NetworkHandler listener, IdentityReport report)
	{
		this.ctx = ctx;
		this.scheduler = scheduler;
		this.listener = listener;
		this.report = report;
	}

	public void store(MQMessage message)
	{
		MessageResendTimer timer = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		Integer packetID = null;
		do
		{
			if (timersMap.size() == MAX_VALUE)
				throw new MQTTException(message.getType(), "outgoing identifier overflow");

			packetID = currID.incrementAndGet();
			if (packetID == MAX_VALUE)
			{
				currID.set(FIRST_ID);
				packetID = FIRST_ID;
			}
		}
		while (timersMap.putIfAbsent(packetID, timer) != null);

		CountableMessage countable = (CountableMessage) message;
		countable.setPacketID(packetID);

		scheduler.store(timer.getRealTimestamp(), timer);
	}

	public MessageResendTimer store(Pubrel message)
	{
		MessageResendTimer timer = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		MessageResendTimer oldTimer = timersMap.get(message.getPacketID());
		if (oldTimer != null)
		{
			oldTimer.stop();
			timersMap.put(message.getPacketID(), timer);
		}
		return oldTimer;
	}

	public MessageResendTimer retrieveConnect()
	{
		return connect;
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
		if (this.connect != null)
			connect.stop();

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
		if (connect != null)
			connect.stop();
		connect = new MessageResendTimer(ctx, scheduler, listener, message, ctx.getResendInterval(), report);
		scheduler.store(connect.getRealTimestamp(), connect);
	}

	public void restartPing()
	{
		if (ctx.getKeepalive() > 0)
		{
			if (this.ping == null)
				this.ping = new MessageResendTimer(ctx, scheduler, listener, MQParser.PINGREQ, (long) ctx.getKeepalive() * 1000, report);
			else
				this.ping.restart();

			scheduler.store(ping.getRealTimestamp(), ping);
		}
	}

	public void stopPing()
	{
		if (this.ping != null)
			this.ping.stop();
	}
}
