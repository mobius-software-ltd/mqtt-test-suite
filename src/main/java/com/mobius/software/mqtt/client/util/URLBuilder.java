package com.mobius.software.mqtt.client.util;

import java.net.URI;

public class URLBuilder
{
	private static final String SEPARATOR = "/";

	public static String build(URI baseURI, PathSegment... segments)
	{
		if (segments == null || segments.length == 0)
			throw new IllegalArgumentException("please specify valid url");
		StringBuilder sb = new StringBuilder();
		sb.append(baseURI);
		for (int i = 0; i < segments.length; i++)
		{
			sb.append(segments[i].getPath());
			if (i < segments.length - 1)
				sb.append(SEPARATOR);
		}
		return sb.toString();
	}
}
