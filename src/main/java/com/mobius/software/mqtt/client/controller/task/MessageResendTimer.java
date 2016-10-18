package com.mobius.software.mqtt.client.controller.task;

import com.mobius.software.mqtt.client.IdentityReport;
import com.mobius.software.mqtt.client.api.data.ConnectionContext;
import com.mobius.software.mqtt.client.controller.PeriodicQueuedTasks;
import com.mobius.software.mqtt.client.net.NetworkListener;
import com.mobius.software.mqtt.parser.header.api.MQMessage;
import com.mobius.software.mqtt.parser.header.impl.MessageType;
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
}
