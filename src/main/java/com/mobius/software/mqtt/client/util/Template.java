package com.mobius.software.mqtt.client.util;

import java.util.ArrayList;
import java.util.List;

public enum Template
{
	IDENTITY
	{
		@Override
		String getTemplate()
		{
			return "identity";
		}
	},
	ACCOUNT
	{
		@Override
		String getTemplate()
		{
			return "account";
		}
	},
	SERVER
	{
		@Override
		String getTemplate()
		{
			return "server";
		}
	};

	abstract String getTemplate();

	public static List<String> list()
	{
		List<String> list = new ArrayList<>();
		for (Template template : values())
			list.add(template.getTemplate());
		return list;
	}
}
