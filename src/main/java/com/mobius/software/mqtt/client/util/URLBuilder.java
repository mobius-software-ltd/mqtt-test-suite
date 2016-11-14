package com.mobius.software.mqtt.client.util;

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

public class URLBuilder
{
	private static final String SEPARATOR = "/";
	private static final String prefix = "http://";
	private static final String addressSeparator = ":";

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

	public static String build(String hostname, Integer port, PathSegment... segments)
	{
		if (hostname == null || port == null)
			throw new IllegalArgumentException("please specify valid controller hostname and port");

		if (segments == null || segments.length == 0)
			throw new IllegalArgumentException("please specify valid url");

		StringBuilder sb = new StringBuilder();
		String baseURI = buildBaseURI(hostname, port);
		sb.append(baseURI);
		for (int i = 0; i < segments.length; i++)
		{
			sb.append(segments[i].getPath());
			if (i < segments.length - 1)
				sb.append(SEPARATOR);
		}
		return sb.toString();
	}

	public static String build(String baseURI, PathSegment... segments)
	{
		if (baseURI == null)
			throw new IllegalArgumentException("please specify valid controller baseURI");

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

	public static String buildBaseURI(String hostname, Integer port)
	{
		return prefix + hostname + addressSeparator + port + SEPARATOR;
	}
}
