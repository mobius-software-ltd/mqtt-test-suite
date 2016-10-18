package com.mobius.software.mqtt.client.controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mobius.software.mqtt.client.controller.task.MessageResendTimer;
import com.mobius.software.mqtt.client.controller.task.Timer;

public class PeriodicQueuedTasks<T extends Timer>
{
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<T>> queues = new ConcurrentHashMap<Long, ConcurrentLinkedQueue<T>>();
	private LinkedBlockingQueue<T> mainQueue;

	private long period;

	public PeriodicQueuedTasks(long period, LinkedBlockingQueue<T> mainQueue)
	{
		this.mainQueue = mainQueue;
		this.period = period;
	}

	public long getPeriod()
	{
		return period;
	}

	public void store(long timestamp, T task)
	{
		Long periodTime = timestamp - timestamp % period;
		ConcurrentLinkedQueue<T> queue = queues.get(periodTime);
		if (queue == null)
		{
			queue = new ConcurrentLinkedQueue<T>();
			ConcurrentLinkedQueue<T> oldQueue = queues.putIfAbsent(periodTime, queue);
			if (oldQueue != null)
				queue = oldQueue;
		}
		queue.offer(task);
	}

	public void executePreviousPool(long timestamp)
	{
		Long periodTime = timestamp - timestamp % period - period;
		ConcurrentLinkedQueue<T> queue = queues.remove(periodTime);
		if (queue != null)
		{
			while (queue.peek() != null)
			{
				T current = queue.poll();
				if (current != null)
				{
					if (current.getRealTimestamp() < timestamp)
						if (current instanceof MessageResendTimer)
							current.execute();
						else
							mainQueue.offer(current);
				}
			}
		}
	}
}