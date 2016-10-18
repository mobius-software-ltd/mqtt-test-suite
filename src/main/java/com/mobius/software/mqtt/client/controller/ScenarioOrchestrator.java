package com.mobius.software.mqtt.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.client.ClientReport;
import com.mobius.software.mqtt.client.IdentityReport;
import com.mobius.software.mqtt.client.PerformanceClient;
import com.mobius.software.mqtt.client.api.json.ResponseData;
import com.mobius.software.mqtt.client.controller.task.Timer;
import com.mobius.software.mqtt.parser.header.impl.MessageType;

public class ScenarioOrchestrator
{
	private PeriodicQueuedTasks<Timer> starter;
	private List<PerformanceClient> clientList;
	private Integer threshold;
	private Integer delay;

	private AtomicInteger pendingCount = new AtomicInteger(0);
	private AtomicInteger completedCount = new AtomicInteger(0);
	private ConcurrentLinkedQueue<PerformanceClient> pendingQueue = new ConcurrentLinkedQueue<>();

	public ScenarioOrchestrator(PeriodicQueuedTasks<Timer> starter, List<PerformanceClient> clientList, Integer threshold, Integer delay)
	{
		this.starter = starter;
		this.clientList = clientList;
		this.threshold = threshold;
		this.delay = delay;
	}

	public void start()
	{
		AtomicInteger startCount = new AtomicInteger(0);
		for (PerformanceClient client : clientList)
		{
			if (startCount.get() < threshold)
			{
				pendingCount.incrementAndGet();
				startCount.incrementAndGet();
				starter.store(client.getTimestamp() + delay, client);
			}
			else
				pendingQueue.offer(client);
		}
	}

	public void notifyOnComplete()
	{
		completedCount.incrementAndGet();
		if (pendingCount.decrementAndGet() < threshold)
		{
			PerformanceClient newClient = pendingQueue.poll();
			if (newClient != null)
			{
				pendingCount.incrementAndGet();
				starter.store(System.currentTimeMillis() + delay, newClient);
			}
		}
	}

	public void terminate()
	{
		//
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
		return new Report(ResponseData.SUCCESS, null, messagesSent, messagesReceived, reports, completedCount.get());
	}
}
