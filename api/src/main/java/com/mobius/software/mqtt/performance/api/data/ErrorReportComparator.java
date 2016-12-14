package com.mobius.software.mqtt.performance.api.data;

import java.util.Comparator;

public class ErrorReportComparator implements Comparator<ErrorReport>
{
	@Override
	public int compare(ErrorReport o1, ErrorReport o2)
	{
		if (o2 == null)
			return 1;
		if (o1 == null)
			return -1;
		if (o2.getTimestamp() == null)
			return 1;
		if (o1.getTimestamp() == null)
			return -1;
		if (o1.getTimestamp().longValue() > o2.getTimestamp().longValue())
			return 1;
		else
			return -1;
	}
}
