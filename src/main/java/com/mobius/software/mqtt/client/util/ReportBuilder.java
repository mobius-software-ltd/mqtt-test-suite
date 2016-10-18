package com.mobius.software.mqtt.client.util;

import java.util.List;

import com.mobius.software.mqtt.client.PerformanceClient;
import com.mobius.software.mqtt.client.api.data.ConnectionProperties;

public class ReportBuilder
{
	private static final String KEY_SEPARATOR = " - ";
	private static final String VALUE_SEPARATOR = ":";
	private static final String NEW_LINE = System.getProperty("line.separator");

	public static String build(ConnectionProperties properties, List<PerformanceClient> clients)
	{
		StringBuilder sb = new StringBuilder();
		return sb.toString();
	}
}
