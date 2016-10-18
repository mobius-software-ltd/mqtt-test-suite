package com.mobius.software.mqtt.client.util;

public enum PathSegment
{
	SCENARIO
	{
		@Override
		public String getPath()
		{
			return "scenario";
		}
	},
	CONTROLLER
	{
		@Override
		public String getPath()
		{
			return "controller";
		}
	},
	REPORT
	{
		@Override
		public String getPath()
		{
			return "report";
		}
	},
	CLEAR
	{
		@Override
		public String getPath()
		{
			return "clear";
		}
	};

	public abstract String getPath();
}
