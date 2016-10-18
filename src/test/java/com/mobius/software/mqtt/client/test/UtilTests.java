package com.mobius.software.mqtt.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.mqtt.client.api.json.ScenarioRequest;

public class UtilTests
{
	@Test
	public void testScenarioRequest()
	{
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("publish.json").getFile());
		ObjectMapper mapper = new ObjectMapper();
		try
		{
			ScenarioRequest request = mapper.readValue(file, ScenarioRequest.class);
			assertNotNull(request);
			assertTrue(request.validate());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
