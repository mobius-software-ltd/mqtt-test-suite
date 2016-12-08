package com.mobius.software.mqtt.client.controller.task;

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

import com.mobius.software.mqtt.client.IdentityReport;
import com.mobius.software.mqtt.client.api.data.ConnectionContext;
import com.mobius.software.mqtt.client.controller.PeriodicQueuedTasks;
import com.mobius.software.mqtt.client.net.NetworkListener;
import com.mobius.software.mqtt.parser.avps.MessageType;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.Publish;

public class MessageResendTimer implements Timer
{
	private ConnectionContext ctx;
	private PeriodicQueuedTasks<Timer> scheduler;
	private NetworkListener listener;
	private MQMessage message;
	private Long timestamp;
	private Long resendInterval;
	private IdentityReport report;

	public MessageResendTimer(ConnectionContext ctx, PeriodicQueuedTasks<Timer> scheduler, NetworkListener listener, MQMessage message, Long resendInterval, IdentityReport report)
	{
		this.ctx = ctx;
		this.scheduler = scheduler;
		this.message = message;
		this.listener = listener;
		this.resendInterval = resendInterval;
		this.timestamp = System.currentTimeMillis() + resendInterval;
		this.report = report;
	}

	@Override
	public Boolean execute()
	{
		if (message.getType() == MessageType.PUBLISH)
		{
			Publish publish = (Publish) message;
			publish.setDup(true);
			report.countDuplicateOut();
		}
		report.countOut(message.getType());
		listener.send(ctx.localAddress(), message);
		timestamp = System.currentTimeMillis() + resendInterval;
		scheduler.store(timestamp, this);
		return true;
	}

	@Override
	public Long getRealTimestamp()
	{
		return timestamp;
	}
	
	@Override
	public void stop()
	{
		this.timestamp = Long.MAX_VALUE;
	}

	public MQMessage retrieveMessage()
	{
		return message;
	}

	public void restart()
	{
		this.timestamp = System.currentTimeMillis() + resendInterval;
	}
}
