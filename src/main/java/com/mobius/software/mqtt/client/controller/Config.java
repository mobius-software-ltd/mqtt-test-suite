package com.mobius.software.mqtt.client.controller;

import java.net.URI;
import java.util.Arrays;

public class Config
{
	private static final String ERROR_MESSAGE = "An eror occured while parsing argument: ";

	private static final String BASE_URI = "-baseURI=";
	private static final String WORKERS = "-workers=";
	private static final String TIMERS_INTERVAL = "-timersInterval=";
	private static final String INITIAL_DELAY = "-delay=";
	
	private URI baseURI;
	private Integer workers;
	private Integer timersInterval;
	private Integer initialDelay;
	
	public Config(URI baseURI, Integer workers, Integer timersInterval, Integer initialDelay)
	{
		this.baseURI = baseURI;
		this.workers = workers;
		this.timersInterval = timersInterval;
		this.initialDelay = initialDelay;		
	}

	public static Config parse(String[] args)
	{
		Integer workers = null, timersInterval = null, initialDelay = null,startThreshold;
		URI baseURI = null;
		try
		{
			if (!args[0].startsWith(BASE_URI))
				throw new IllegalArgumentException(ERROR_MESSAGE + BASE_URI + ", args: " + Arrays.asList(args));
			baseURI = URI.create(args[0].replace(BASE_URI, ""));

			if (!args[1].startsWith(WORKERS))
				throw new IllegalArgumentException(ERROR_MESSAGE + WORKERS + ", args: " + Arrays.asList(args));
			workers = Integer.parseInt(args[1].replace(WORKERS, ""));

			if (!args[2].startsWith(TIMERS_INTERVAL))
				throw new IllegalArgumentException(ERROR_MESSAGE + TIMERS_INTERVAL + ", args: " + Arrays.asList(args));
			timersInterval = Integer.parseInt(args[2].replace(TIMERS_INTERVAL, ""));

			if (!args[3].startsWith(INITIAL_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + INITIAL_DELAY + ", args: " + Arrays.asList(args));
			initialDelay = Integer.parseInt(args[3].replace(INITIAL_DELAY, ""));						
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("invalid arguments format: " + e.getMessage());
		}

		return new Config(baseURI, workers, timersInterval, initialDelay);
	}

	public URI getBaseURI()
	{
		return baseURI;
	}

	public void setBaseURI(URI baseURI)
	{
		this.baseURI = baseURI;
	}

	public Integer getWorkers()
	{
		return workers;
	}

	public void setWorkers(Integer workers)
	{
		this.workers = workers;
	}

	public Integer getTimersInterval()
	{
		return timersInterval;
	}

	public void setTimersInterval(Integer timersInterval)
	{
		this.timersInterval = timersInterval;
	}

	public Integer getInitialDelay()
	{
		return initialDelay;
	}

	public void setInitialDelay(Integer initialDelay)
	{
		this.initialDelay = initialDelay;
	}
}
