package com.mobius.software.mqtt.performance.controller;

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

import java.net.URI;
import java.util.Arrays;

public class Config
{
	private static final String ERROR_MESSAGE = "An eror occured while parsing argument: ";

	private static final String WORKERS = "-workers=";
	private static final String TIMERS_INTERVAL = "-timersInterval=";
	private static final String INITIAL_DELAY = "-delay=";
	
	private Integer workers;
	private Integer timersInterval;
	private Integer initialDelay;
	
	public Config(URI baseURI, Integer workers, Integer timersInterval, Integer initialDelay)
	{
		this.workers = workers;
		this.timersInterval = timersInterval;
		this.initialDelay = initialDelay;		
	}

	public static Config parse(String[] args)
	{
		Integer workers = null, timersInterval = null, initialDelay = null;
		URI baseURI = null;
		try
		{
			if (!args[0].startsWith(WORKERS))
				throw new IllegalArgumentException(ERROR_MESSAGE + WORKERS + ", args: " + Arrays.asList(args));
			workers = Integer.parseInt(args[0].replace(WORKERS, ""));

			if (!args[1].startsWith(TIMERS_INTERVAL))
				throw new IllegalArgumentException(ERROR_MESSAGE + TIMERS_INTERVAL + ", args: " + Arrays.asList(args));
			timersInterval = Integer.parseInt(args[1].replace(TIMERS_INTERVAL, ""));

			if (!args[2].startsWith(INITIAL_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + INITIAL_DELAY + ", args: " + Arrays.asList(args));
			initialDelay = Integer.parseInt(args[2].replace(INITIAL_DELAY, ""));						
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("invalid arguments format: " + e.getMessage());
		}

		return new Config(baseURI, workers, timersInterval, initialDelay);
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
