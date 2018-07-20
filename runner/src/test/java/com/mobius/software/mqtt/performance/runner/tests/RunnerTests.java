package com.mobius.software.mqtt.performance.runner.tests;

import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mobius.software.mqtt.performance.runner.util.TemplateParser;

public class RunnerTests
{
	private static final String MULTI_SUBSCRIBERS_QOS0 = "json/subscribers_qos0.json";
	private static final String MULTI_SUBSCRIBERS_QOS1 = "json/subscribers_qos1.json";
	private static final String MULTI_SUBSCRIBERS_QOS2 = "json/subscribers_qos2.json";
	private static final String MULTI_PUBLISHERS_QOS0 = "json/publishers_qos0.json";
	private static final String MULTI_PUBLISHERS_QOS1 = "json/publishers_qos1.json";
	private static final String MULTI_PUBLISHERS_QOS2 = "json/publishers_qos2.json";
	private static final String PINGERS = "json/pingers.json";

	private static final String WS_MULTI_SUBSCRIBERS_QOS0 = "json/ws_subscribers_qos0.json";
	private static final String WS_MULTI_SUBSCRIBERS_QOS1 = "json/ws_subscribers_qos1.json";
	private static final String WS_MULTI_SUBSCRIBERS_QOS2 = "json/ws_subscribers_qos2.json";
	private static final String WS_MULTI_PUBLISHERS_QOS0 = "json/ws_publishers_qos0.json";
	private static final String WS_MULTI_PUBLISHERS_QOS1 = "json/ws_publishers_qos1.json";
	private static final String WS_MULTI_PUBLISHERS_QOS2 = "json/ws_publishers_qos2.json";
	private static final String WS_PINGERS = "json/ws_pingers.json";

	private static TemplateParser templateParser = new TemplateParser();

	@BeforeClass
	public static void beforeClass()
	{
		fillTemplateRemote();
		//fillTemplateLocal();
	}

	@Test
	public void test1000Subscribers_qos0()
	{
		checkScenario(MULTI_SUBSCRIBERS_QOS0);
		checkScenario(WS_MULTI_SUBSCRIBERS_QOS0);
	}

	@Test
	public void test1000Subscribers_qos1()
	{
		checkScenario(MULTI_SUBSCRIBERS_QOS1);
		checkScenario(WS_MULTI_SUBSCRIBERS_QOS1);
	}

	@Test
	public void test1000Subscribers_qos2()
	{
		checkScenario(WS_MULTI_SUBSCRIBERS_QOS2);
	}

	@Test
	public void test1000Publishers_qos0()
	{
		checkScenario(MULTI_PUBLISHERS_QOS0);
		checkScenario(WS_MULTI_PUBLISHERS_QOS0);
	}

	@Test
	public void test1000Publishers_qos1()
	{
		checkScenario(MULTI_PUBLISHERS_QOS1);
		checkScenario(WS_MULTI_PUBLISHERS_QOS1);
	}

	@Test
	public void test1000Publishers_qos2()
	{
		checkScenario(MULTI_PUBLISHERS_QOS2);
		checkScenario(WS_MULTI_PUBLISHERS_QOS2);
	}

	@Test
	public void test100kConnections()
	{
		checkScenario(PINGERS);
		checkScenario(WS_PINGERS);
	}

	@Test
	public void testAll()
	{
		checkScenario(MULTI_SUBSCRIBERS_QOS0);
		checkScenario(MULTI_SUBSCRIBERS_QOS1);
		checkScenario(MULTI_SUBSCRIBERS_QOS2);
		checkScenario(MULTI_PUBLISHERS_QOS0);
		checkScenario(MULTI_PUBLISHERS_QOS1);
		checkScenario(MULTI_PUBLISHERS_QOS2);
		checkScenario(PINGERS);

		checkScenario(WS_MULTI_SUBSCRIBERS_QOS0);
		checkScenario(WS_MULTI_SUBSCRIBERS_QOS1);
		checkScenario(WS_MULTI_SUBSCRIBERS_QOS2);
		checkScenario(WS_MULTI_PUBLISHERS_QOS0);
		checkScenario(WS_MULTI_PUBLISHERS_QOS1);
		checkScenario(WS_MULTI_PUBLISHERS_QOS2);
		checkScenario(WS_PINGERS);
	}

	private void checkScenario(String filename)
	{
		try
		{
			runScenario(filename);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	private TestRunner runScenario(String scenarioFilename) throws InterruptedException
	{
		TestRunner runner = new TestRunner(scenarioFilename, templateParser);
		runner.start();
		runner.awaitFinished();
		return runner;
	}

	private static void fillTemplateRemote()
	{
		templateParser.addTemplate("{controller.1.ip}", "137.117.200.108");
		templateParser.addTemplate("{controller.1.port}", "9998");
		templateParser.addTemplate("{controller.2.ip}", "13.81.243.219");
		templateParser.addTemplate("{controller.2.port}", "9998");
		templateParser.addTemplate("{controller.3.ip}", "13.81.244.166");
		templateParser.addTemplate("{controller.3.port}", "9998");
		templateParser.addTemplate("{mqtt.lb.ip}", "13.94.158.185");
		templateParser.addTemplate("{mqtt.lb.port}", "1883");
		templateParser.addTemplate("{mqtt.ws.port}", "21883");
	}

	private static void fillTemplateLocal()
	{
		templateParser.addTemplate("{controller.1.ip}", "127.0.0.1");
		templateParser.addTemplate("{controller.1.port}", "9998");
		templateParser.addTemplate("{controller.2.ip}", "127.0.0.1");
		templateParser.addTemplate("{controller.2.port}", "9998");
		templateParser.addTemplate("{mqtt.lb.ip}", "127.0.2.1");
		templateParser.addTemplate("{mqtt.lb.port}", "1883");
		templateParser.addTemplate("{mqtt.ws.port}", "21883");
	}
}
