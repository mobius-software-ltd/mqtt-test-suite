package com.mobius.software.mqtt.performance.commons.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class Mark
{
	private static ConcurrentLinkedQueue<Measurement> measurementCach = new ConcurrentLinkedQueue<>();
	private static ConcurrentLinkedQueue<List<Measurement>> timestampsListCache = new ConcurrentLinkedQueue<List<Measurement>>();
	static
	{
		for (int i = 0; i < Measurements.cacheSize; i++)
		{
			measurementCach.offer(new Measurement());
			timestampsListCache.offer(Collections.synchronizedList(new ArrayList<Measurement>()));
		}
	}

	private final String name;
	private final TimeUnit unit;
	private ConcurrentHashMap<String, List<Measurement>> measurements = new ConcurrentHashMap<>();

	private Mark(String name, TimeUnit unit)
	{
		this.name = name;
		this.unit = unit;
	}

	public void start()
	{
		long start = markTimestamp();
		String currThreadName = Thread.currentThread().getName();
		List<Measurement> marks = pollMarks(currThreadName);
		Measurement mark = marks.get(marks.size() - 1);
		mark.setStart(start);
	}

	public void stop()
	{
		long stop = markTimestamp();
		String currThreadName = Thread.currentThread().getName();
		List<Measurement> measurements = this.measurements.get(currThreadName);
		if (measurements == null || measurements.isEmpty())
			throw new IllegalStateException("Invalid usage: can't place stop mark before start!");
		Measurement measurement = measurements.get(measurements.size() - 1);
		measurement.setStop(stop);
	}

	private long markTimestamp()
	{
		long timestamp = 0L;
		switch (unit)
		{
		case SECONDS:
			timestamp = unit.toSeconds(System.currentTimeMillis());
			break;
		case MILLISECONDS:
			timestamp = System.currentTimeMillis();
			break;
		case MICROSECONDS:
			timestamp = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
			break;
		case NANOSECONDS:
			timestamp = System.nanoTime();
			break;
		default:
			break;
		}
		return timestamp;
	}

	private List<Measurement> pollMarks(String name)
	{
		List<Measurement> startMeasurements = measurements.get(name);
		if (startMeasurements == null)
		{
			startMeasurements = timestampsListCache.poll();
			if (startMeasurements == null)
				startMeasurements = Collections.synchronizedList(new ArrayList<Measurement>());
			List<Measurement> oldMeasurements = measurements.putIfAbsent(name, startMeasurements);
			if (oldMeasurements != null)
				startMeasurements = oldMeasurements;
		}
		startMeasurements.add(fetchMeasurement());
		return startMeasurements;
	}

	private Measurement fetchMeasurement()
	{
		Measurement measurement = measurementCach.poll();
		if (measurement == null)
			measurement = new Measurement();
		return measurement;
	}

	public long min()
	{
		if (measurements.isEmpty())
			return 0L;

		long min = Long.MAX_VALUE;
		for (List<Measurement> list : measurements.values())
		{
			for (Measurement measurement : list)
			{
				long diff = measurement.diff();
				if (diff != -1)
				{
					if (diff < min)
						min = diff;
					if (min == 0)
						return min;
				}
			}
		}
		return min;
	}

	public long max()
	{
		if (measurements.isEmpty())
			return 0L;

		long max = 0L;
		for (List<Measurement> list : measurements.values())
		{
			for (Measurement measurement : list)
			{
				if (measurement.diff() != -1)
				{
					if (measurement.diff() > max)
						max = measurement.diff();
				}
			}
		}
		return max;
	}

	public long avg()
	{
		if (measurements.isEmpty())
			return 0L;

		BigInteger total = BigInteger.ZERO;
		for (List<Measurement> list : measurements.values())
		{
			for (Measurement measurement : list)
			{
				if (measurement.diff() != -1)
					total = total.add(BigInteger.valueOf(measurement.diff()));
			}
		}

		BigInteger avg = total.divide(BigInteger.valueOf(count()));
		return avg.longValue();
	}

	public static Mark seconds(String name)
	{
		return new Mark(name, TimeUnit.SECONDS);
	}

	public static Mark millis(String name)
	{
		return new Mark(name, TimeUnit.MILLISECONDS);
	}

	public static Mark micros(String name)
	{
		return new Mark(name, TimeUnit.MICROSECONDS);
	}

	public static Mark nanos(String name)
	{
		return new Mark(name, TimeUnit.NANOSECONDS);
	}

	public int count()
	{
		int totalMarksMade = 0;
		for (List<Measurement> list : measurements.values())
			totalMarksMade += list.size();
		return totalMarksMade;
	}

	public String getName()
	{
		return name;
	}

	public TimeUnit getUnit()
	{
		return unit;
	}
}
