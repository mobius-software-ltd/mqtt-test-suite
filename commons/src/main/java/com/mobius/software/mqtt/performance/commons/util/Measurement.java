package com.mobius.software.mqtt.performance.commons.util;

public class Measurement
{
	private long start;
	private long stop;

	public long getStart()
	{
		return start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public long getStop()
	{
		return stop;
	}

	public void setStop(long stop)
	{
		this.stop = stop;
	}

	public long diff()
	{
		if (start == 0 || stop == 0)
			return -1;
		return stop - start;
	}
}