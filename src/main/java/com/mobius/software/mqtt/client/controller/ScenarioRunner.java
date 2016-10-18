package com.mobius.software.mqtt.client.controller;

import java.util.concurrent.LinkedBlockingQueue;

import com.mobius.software.mqtt.client.controller.task.Timer;

public class ScenarioRunner implements Runnable
{
	private LinkedBlockingQueue<Timer> queue;
	private PeriodicQueuedTasks<Timer> scheduler;

	public ScenarioRunner(PeriodicQueuedTasks<Timer> scheduler, LinkedBlockingQueue<Timer> queue)
	{
		this.scheduler = scheduler;
		this.queue = queue;
	}

	@Override
	public void run()
	{
		while (true)
		{
			Timer timer = null;
			try
			{
				timer = queue.take();
			}
			catch (InterruptedException ex)
			{

			}

			try
			{
				if (timer != null && timer.getRealTimestamp() < System.currentTimeMillis() && timer.execute())
					scheduler.store(timer.getRealTimestamp(), timer);				
			}
			catch (Exception ex)
			{
			}
		}
	}
}
