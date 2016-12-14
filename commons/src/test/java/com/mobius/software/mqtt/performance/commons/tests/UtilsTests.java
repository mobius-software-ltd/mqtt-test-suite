package com.mobius.software.mqtt.performance.commons.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.mobius.software.mqtt.performance.commons.util.IdentifierParser;

public class UtilsTests
{
	@Test
	public void testParseIdentifier()
	{
		try
		{
			String username = "foo@bar.net";
			String server = "127.0.0.1";
			Integer identifier = 123;

			String regex = "%identity%";
			String clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals("123", clientID);

			regex = "%identity%account%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(identifier + "_" + username, clientID);

			regex = "%identity%server%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(identifier + "_" + server, clientID);

			regex = "%account%identity%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(username + "_" + identifier, clientID);

			regex = "%server%identity%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(server + "_" + identifier, clientID);

			regex = "%server%account%identity%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(server + "_" + username + "_" + identifier, clientID);

			regex = "%account%server%identity%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(username + "_" + server + "_" + identifier, clientID);

			regex = "%server%identity%account%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(server + "_" + identifier + "_" + username, clientID);

			regex = "%account%identity%server%";
			clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			assertEquals(username + "_" + identifier + "_" + server, clientID);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testParseIdentityCounter()
	{
		try
		{
			String regex = "%account%identity%";
			String username = "foo@bar.net";
			String server = "127.0.0.1";
			Integer identifier = 123;
			String clientID = IdentifierParser.parseIdentifier(regex, username, server, identifier);
			Integer actual = IdentifierParser.parseIdentifierCounter(clientID);
			assertNotNull(actual);
			assertEquals(identifier, actual);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
