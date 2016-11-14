package com.mobius.software.mqtt.client.controller;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.client.IdentityReport;
import com.mobius.software.mqtt.client.PerformanceClient;
import com.mobius.software.mqtt.client.api.data.ClientReport;
import com.mobius.software.mqtt.client.api.json.ResponseData;
import com.mobius.software.mqtt.client.controller.task.Timer;
import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class ScenarioOrchestrator
{
	private PeriodicQueuedTasks<Timer> scheduler;
	private List<PerformanceClient> clientList;
	private Integer threshold;
	private Integer startThreshold;
	private Integer delay;

	private AtomicInteger startingCount = new AtomicInteger(0);
	private AtomicInteger pendingCount = new AtomicInteger(0);
	private AtomicInteger completedCount = new AtomicInteger(0);
	private ConcurrentLinkedQueue<PerformanceClient> pendingQueue = new ConcurrentLinkedQueue<>();

	public ScenarioOrchestrator(PeriodicQueuedTasks<Timer> scheduler, List<PerformanceClient> clientList, Integer threshold, Integer startThreshold, Integer delay)
	{
		this.scheduler = scheduler;
		this.clientList = clientList;
		this.threshold = threshold;
		this.startThreshold = startThreshold;
		this.delay = delay;
	}

	public void start()
	{
		for (PerformanceClient client : clientList)
		{
			if (startingCount.get() < startThreshold)
			{
				pendingCount.incrementAndGet();
				startingCount.incrementAndGet();
				scheduler.store(System.currentTimeMillis() + delay, client);
			}
			else
				pendingQueue.offer(client);
		}
	}

	public void notifyOnStart()
	{
		if (startingCount.decrementAndGet() < startThreshold)
		{
			if (pendingCount.get() < threshold)
			{
				PerformanceClient newClient = pendingQueue.poll();
				if (newClient != null)
				{
					pendingCount.incrementAndGet();
					startingCount.incrementAndGet();
					scheduler.store(System.currentTimeMillis() + delay, newClient);
				}
			}
		}
	}

	public void notifyOnComplete()
	{
		completedCount.incrementAndGet();
		if (pendingCount.decrementAndGet() < threshold)
		{
			if (startingCount.get() < startThreshold)
			{
				PerformanceClient newClient = pendingQueue.poll();
				if (newClient != null)
				{
					pendingCount.incrementAndGet();
					startingCount.incrementAndGet();
					scheduler.store(System.currentTimeMillis() + delay, newClient);
				}
			}
		}
	}

	public void terminate()
	{
		for (PerformanceClient client : clientList)
			client.stop();
	}

	public Report report()
	{
		long messagesSent = 0, messagesReceived = 0;
		List<ClientReport> reports = new ArrayList<>();
		for (PerformanceClient client : clientList)
		{
			IdentityReport identityReport = client.getReport();
			AtomicInteger publishInCounter = identityReport.getOutPacketCounters().get(MessageType.PUBLISH);
			if (publishInCounter != null)
				messagesReceived += publishInCounter.get();
			AtomicInteger publishOutCounter = identityReport.getOutPacketCounters().get(MessageType.PUBLISH);
			if (publishOutCounter != null)
				messagesSent += publishOutCounter.get();
			reports.add(ClientReport.valueOf(client.getReport()));
		}
		return new Report(ResponseData.SUCCESS, messagesSent, messagesReceived, reports, completedCount.get());
	}
}
