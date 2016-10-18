package com.mobius.software.mqtt.client.controller.task;

import com.mobius.software.mqtt.client.api.data.ClientIdentifier;

public interface ClientTask extends Task
{
	ClientIdentifier retrieveClientIdentifier();

	boolean isPreviousFinished();
}
