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

package com.mobius.software.mqtt.performance.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.performance.api.json.ReportResponse;
import com.mobius.software.mqtt.performance.api.json.ResponseData;
import com.mobius.software.mqtt.performance.commons.data.ClientReport;
import com.mobius.software.mqtt.performance.controller.client.Client;
import com.mobius.software.mqtt.performance.controller.task.TimedTask;

public class Orchestrator
{
	private OrchestratorProperties properties;
	private PeriodicQueuedTasks<TimedTask> scheduler;
	private List<Client> clientList;

	private AtomicInteger startingCount = new AtomicInteger(0);
	private AtomicInteger pendingCount = new AtomicInteger(0);
	private ConcurrentLinkedQueue<Client> pendingQueue = new ConcurrentLinkedQueue<>();
	private long startTime;
	private long finishTime;

	public Orchestrator(OrchestratorProperties properties, PeriodicQueuedTasks<TimedTask> scheduler, List<Client> clientList)
	{
		this.properties = properties;
		this.scheduler = scheduler;
		this.clientList = clientList;
	}

	public void start()
	{
		startTime = System.currentTimeMillis() + properties.getScenarioDelay();
		Queue<Client> tempQueue = new LinkedList<>(clientList);
		Map<Client, Long> toStore = new LinkedHashMap<>();
		while (!tempQueue.isEmpty())
		{
			int oldStarting = startingCount.get();
			int newStarting = startingCount.updateAndGet(v -> startingCount.get() >= properties.getStartThreashold() ? startingCount.get() : startingCount.get() + 1);
			if (newStarting > oldStarting)
				toStore.put(tempQueue.poll(), System.currentTimeMillis() + properties.getInitialDelay() + properties.getScenarioDelay());
			else
				break;
		}

		pendingQueue.addAll(tempQueue);
		for (Entry<Client, Long> entry : toStore.entrySet())
			scheduler.store(entry.getValue(), entry.getKey());
	}

	public void notifyOnStart()
	{
		if (startingCount.decrementAndGet() < properties.getStartThreashold())
		{
			int oldPending = startingCount.get();
			int newPending = startingCount.updateAndGet(v -> !pendingQueue.isEmpty() && startingCount.get() >= properties.getStartThreashold() ? startingCount.get() : startingCount.get() + 1);
			if (newPending > oldPending)
			{
				Client newClient = pendingQueue.poll();
				if (newClient != null)
				{
					pendingCount.incrementAndGet();
					scheduler.store(System.currentTimeMillis() + properties.getInitialDelay(), newClient);
				}
			}
		}
	}

	public void notifyOnComplete()
	{
		if (pendingCount.decrementAndGet() < properties.getThreashold())
		{
			int oldPending = pendingCount.get();
			int newPending = pendingCount.updateAndGet(v -> !pendingQueue.isEmpty() && pendingCount.get() >= properties.getThreashold() ? pendingCount.get() : pendingCount.get() + 1);
			if (newPending > oldPending)
			{
				Client newClient = pendingQueue.poll();
				if (newClient != null)
					scheduler.store(System.currentTimeMillis() + properties.getInitialDelay(), newClient);
				else
					finishTime = System.currentTimeMillis();
			}
		}
	}

	public void terminate()
	{
		for (Client client : clientList)
			client.stop();
	}

	public ReportResponse report()
	{
		List<ClientReport> reports = new ArrayList<>();
		for (Client client : clientList)
			reports.add(client.retrieveReport().translate());
		return new ReportResponse(ResponseData.SUCCESS, properties.getScenarioID(), startTime, finishTime, reports);
	}

	public OrchestratorProperties getProperties()
	{
		return properties;
	}

	public PeriodicQueuedTasks<TimedTask> getScheduler()
	{
		return scheduler;
	}
}
