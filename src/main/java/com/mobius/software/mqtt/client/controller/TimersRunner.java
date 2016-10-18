package com.mobius.software.mqtt.client.controller;

import com.mobius.software.mqtt.client.controller.task.Timer;

public class TimersRunner implements Runnable
{
	private PeriodicQueuedTasks<Timer> timers;

	public TimersRunner(PeriodicQueuedTasks<Timer> timers)
	{
		this.timers = timers;
	}

	@Override
	public void run()
	{
		try
		{
			timers.executePreviousPool(System.currentTimeMillis());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
