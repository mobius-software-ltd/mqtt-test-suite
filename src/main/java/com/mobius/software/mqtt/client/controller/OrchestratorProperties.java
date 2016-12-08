package com.mobius.software.mqtt.client.controller;

import java.net.InetSocketAddress;

import com.mobius.software.mqtt.client.api.data.ScenarioProperties;

public class OrchestratorProperties
{
	private String serverHostname;
	private int serverPort;
	private int threashold;
	private int startThreashold;
	private long resendInterval;
	private long minPingInterval;
	private boolean continueOnError;
	private int initialDelay;
	private String identifierRegex;
	private int startIdentifier;

	private InetSocketAddress serverAddress;

	private OrchestratorProperties(String serverHostname, int serverPort, int threashold, int startThreashold, long resendInterval, long minPingInterval, boolean continueOnError, int initialDelay, String identifierRegex, int startIdentifier, InetSocketAddress serverAddress)
	{
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.threashold = threashold;
		this.startThreashold = startThreashold;
		this.resendInterval = resendInterval;
		this.minPingInterval = minPingInterval;
		this.continueOnError = continueOnError;
		this.initialDelay = initialDelay;
		this.identifierRegex = identifierRegex;
		this.startIdentifier = startIdentifier;
		this.serverAddress = serverAddress;
	}

	public static OrchestratorProperties fromScenarioProperties(ScenarioProperties scenarioProperties, Integer threashold, Integer startThreashold, boolean continueOnError, Integer initialDelay)
	{
		InetSocketAddress serverAddress = new InetSocketAddress(scenarioProperties.getServerHostname(), scenarioProperties.getServerPort());
		return new OrchestratorProperties(scenarioProperties.getServerHostname(), scenarioProperties.getServerPort(), threashold, startThreashold, scenarioProperties.getResendInterval(), scenarioProperties.getMinPingInterval(), continueOnError, initialDelay, scenarioProperties.getIdentifierRegex(), scenarioProperties.getStartIdentifier(), serverAddress);
	}

	public String getServerHostname()
	{
		return serverHostname;
	}

	public int getServerPort()
	{
		return serverPort;
	}

	public int getThreashold()
	{
		return threashold;
	}

	public int getStartThreashold()
	{
		return startThreashold;
	}

	public long getResendInterval()
	{
		return resendInterval;
	}

	public long getMinPingInterval()
	{
		return minPingInterval;
	}

	public boolean isContinueOnError()
	{
		return continueOnError;
	}

	public int getInitialDelay()
	{
		return initialDelay;
	}

	public String getIdentifierRegex()
	{
		return identifierRegex;
	}

	public int getStartIdentifier()
	{
		return startIdentifier;
	}

	public InetSocketAddress getServerAddress()
	{
		return serverAddress;
	}
}
