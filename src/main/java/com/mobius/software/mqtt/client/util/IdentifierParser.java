package com.mobius.software.mqtt.client.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentifierParser
{
	private static final String REGEX_SEPARATOR = "%";
	private static final String IDENTIFIER_SEPARATOR = "_";

	private static final ConcurrentHashMap<String, AtomicInteger> identifiersMap = new ConcurrentHashMap<>();

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

	public static String parseIdentifier(String regex, String username)
	{
		if (identifiersMap.get(username) == null)
			identifiersMap.putIfAbsent(username, new AtomicInteger());
		String identityNumber = String.valueOf(identifiersMap.get(username).incrementAndGet());

		StringBuilder sb = new StringBuilder();
		List<String> segments = Arrays.asList(regex.split(REGEX_SEPARATOR));
		for (String segment : segments)
		{
			segment.replaceAll(REGEX_SEPARATOR, "");

			if (segment.equals(Template.IDENTITY.getTemplate()))
				sb.append(identityNumber).append(IDENTIFIER_SEPARATOR);
			else if (segment.equals(Template.ACCOUNT.getTemplate()))
				sb.append(username).append(IDENTIFIER_SEPARATOR);
			else if (!segment.isEmpty())
				throw new IllegalArgumentException("invalid regex expression");
		}

		String identifier = sb.toString();
		identifier = identifier.substring(0, identifier.length() - 1);
		return identifier;
	}

	public static boolean validate(String regex)
	{
		List<String> segments = Arrays.asList(regex.split(REGEX_SEPARATOR));
		List<String> templates = Template.list();
		return !Collections.disjoint(segments, templates);
	}
}
