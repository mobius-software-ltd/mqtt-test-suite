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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentifierParser
{
	private static final String REGEX_SEPARATOR = "%";
	private static final String IDENTIFIER_SEPARATOR = "_";

	private static ConcurrentHashMap<String, AtomicInteger> identifiers = new ConcurrentHashMap<>();

	public static String parseIdentifier(String regex, String username, String server, Integer startIdentifier)
	{
		StringBuilder sb = new StringBuilder();
		List<String> segments = Arrays.asList(regex.split(REGEX_SEPARATOR));
		for (String segment : segments)
		{
			segment.replaceAll(REGEX_SEPARATOR, "");
			if (segment.equals(Template.IDENTITY.getTemplate()))
			{
				if (!identifiers.containsKey(regex))
					identifiers.putIfAbsent(regex, new AtomicInteger(startIdentifier));
				AtomicInteger currIdentifier = identifiers.get(regex);
				String identityNumber = String.valueOf(currIdentifier.getAndIncrement());
				sb.append(identityNumber).append(IDENTIFIER_SEPARATOR);
			}
			else if (segment.equals(Template.ACCOUNT.getTemplate()))
				sb.append(username).append(IDENTIFIER_SEPARATOR);
			else if (segment.equals(Template.SERVER.getTemplate()))
				sb.append(server).append(IDENTIFIER_SEPARATOR);
			else if (!segment.isEmpty())
				throw new IllegalArgumentException("invalid regex expression");
		}
		String identifier = sb.toString();
		return identifier.substring(0, identifier.length() - 1);
	}

	public static String createTemplate(Template... args)
	{
		if (args == null || args.length == 0)
			throw new IllegalArgumentException("please supply at least one template parameter");
		StringBuilder sb = new StringBuilder();
		for (Template arg : args)
			sb.append(REGEX_SEPARATOR).append(arg.getTemplate());
		sb.append(REGEX_SEPARATOR);
		return sb.toString();
	}

	public static boolean validate(String regex)
	{
		List<String> segments = Arrays.asList(regex.split(REGEX_SEPARATOR));
		List<String> templates = Template.list();
		return !Collections.disjoint(segments, templates);
	}
}
