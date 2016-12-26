package com.mobius.software.mqtt.performance.commons.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.mobius.software.mqtt.performance.commons.util.table.Cell;
import com.mobius.software.mqtt.performance.commons.util.table.TableBuilder;

public class Measurements
{
	private static final int TABLE_WIDTH = 130;

	private static final String HEADER = "Measurements";
	private static final String START_TIME = "Start Time:";
	private static final String CURRENT_TIME = "Current Time:";
	private static final String NAME = "Marker Name";
	private static final String MIN = "MIN";
	private static final String AVG = "AVG";
	private static final String MAX = "MAX";
	private static final String TIME_UNIT = "Time Unit";
	private static final String COUNT = "Count";
	private static final String EMPTY_STRING = "";

	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	private static long startTime = System.currentTimeMillis();
	private static ConcurrentHashMap<String, Mark> markers = new ConcurrentHashMap<>();

	public static int cacheSize = 10000;

	public static void init(int initCahceSize, Mark... marks)
	{
		cacheSize = initCahceSize;
		register(marks);
	}

	public static void register(Mark... marks)
	{
		for (Mark mark : marks)
			markers.put(mark.getName(), mark);
	}

	public static void start(String name)
	{
		Mark mark = markers.get(name);
		if (mark == null)
			throw new IllegalArgumentException(name + " marker was not initialized with " + Measurements.class.getSimpleName() + ". \n For proper usage - pass it to " + Measurements.class.getSimpleName() + ".init method");
		mark.start();
	}

	public static void stop(String name)
	{
		Mark mark = markers.get(name);
		if (mark == null)
			throw new IllegalArgumentException(name + " marker was not initialized with " + Measurements.class.getSimpleName() + ". \n For proper usage - pass it to " + Measurements.class.getSimpleName() + ".init method");
		mark.stop();
	}

	public static void print()
	{
		String table = getStatistics();
		System.out.println(table);
	}

	public static void writeToFile(String filePath) throws IOException
	{
		String content = getStatistics();
		if (content.isEmpty())
			return;

		File file = new File(filePath);
		if (!file.exists())
			file.createNewFile();

		try (PrintWriter pw = new PrintWriter(file))
		{
			pw.println(content);
		}
	}

	public static String getStatistics()
	{
		if (markers.isEmpty())
			return EMPTY_STRING;

		Long currTime = System.currentTimeMillis();
		TableBuilder builder = new TableBuilder().width(TABLE_WIDTH);
		builder.addHeader(Cell.center(HEADER));
		builder.addRow(Cell.left(START_TIME), Cell.left(timestampToDateTime(startTime)), Cell.left(startTime), Cell.empty());
		builder.addRow(Cell.left(CURRENT_TIME), Cell.left(timestampToDateTime(currTime)), Cell.left(currTime), Cell.empty());
		builder.addFooter(4);
		builder.addRow(Cell.center(NAME), Cell.center(TIME_UNIT), Cell.center(MIN), Cell.center(AVG), Cell.center(MAX), Cell.center(COUNT));
		builder.addFooter(6);
		for (Mark mark : markers.values())
		{
			builder.addRow(Cell.center(mark.getName()), Cell.center(mark.getUnit().name()), Cell.center(mark.min()), Cell.center(mark.avg()), Cell.center(mark.max()), Cell.center(mark.count()));
		}
		builder.addFooter(6);

		return builder.build();
	}

	private static String timestampToDateTime(long timestamp)
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat(DATE_TIME_FORMAT);
		Date now = new Date(timestamp);
		String strDate = sdfDate.format(now);
		return strDate;
	}
}
