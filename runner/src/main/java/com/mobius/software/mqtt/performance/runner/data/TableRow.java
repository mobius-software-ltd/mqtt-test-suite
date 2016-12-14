package com.mobius.software.mqtt.performance.runner.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableRow
{
	private List<String> columns = new ArrayList<>();

	private TableRow(List<String> columns)
	{
		this.columns = columns;
	}

	public void addColumns(String... columns)
	{
		this.columns.addAll(Arrays.asList(columns));
	}

	public List<String> getColumns()
	{
		return columns;
	}

	public static TableRow forColumns(String... columns)
	{
		return new TableRow(Arrays.asList(columns));
	}
}
