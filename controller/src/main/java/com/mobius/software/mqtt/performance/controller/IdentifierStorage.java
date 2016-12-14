package com.mobius.software.mqtt.performance.controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import com.mobius.software.mqtt.performance.commons.util.IdentifierParser;

public class IdentifierStorage
{
	private static final Integer START_VALUE = 0;
	private ConcurrentHashMap<String, ConcurrentSkipListSet<Integer>> usedRegexIdentifiers = new ConcurrentHashMap<>();

	public int countIdentity(String regex, Integer startIdentifier)
	{
		ConcurrentSkipListSet<Integer> usedIdentifiers = usedRegexIdentifiers.get(regex);
		if (usedIdentifiers == null)
		{
			usedIdentifiers = new ConcurrentSkipListSet<Integer>();
			usedIdentifiers.add(START_VALUE);
			usedRegexIdentifiers.putIfAbsent(regex, usedIdentifiers);
		}

		Integer identifier = startIdentifier;
		if (usedIdentifiers.add(identifier))
			return identifier;

		identifier = usedIdentifiers.last();
		do
		{
			if (identifier == Integer.MAX_VALUE)
				identifier = START_VALUE;
			identifier++;
		}
		while (!usedIdentifiers.add(identifier));
		return identifier;
	}

	public void releaseIdentifier(String regex, String clientID)
	{
		ConcurrentSkipListSet<Integer> usedIdentifiers = usedRegexIdentifiers.get(regex);
		if (usedIdentifiers != null)
		{
			Integer identifierCounter = IdentifierParser.parseIdentifierCounter(clientID);
			if (identifierCounter != null)
				usedIdentifiers.remove(identifierCounter);
		}
	}
}
