package com.mobius.software.mqtt.client.controller.task;

public interface Timer extends Task
{
	Long getRealTimestamp();

	void stop();
}
