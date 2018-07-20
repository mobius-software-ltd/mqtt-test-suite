package com.mobius.software.mqtt.performance.runner.tests;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.mobius.software.mqtt.performance.commons.data.ScenarioRequest;
import com.mobius.software.mqtt.performance.runner.ScenarioRunner;
import com.mobius.software.mqtt.performance.runner.util.TemplateParser;

public class TestRunner extends Thread
{
	private String scenarioFilename;
	private CountDownLatch latch = new CountDownLatch(1);
	private TemplateParser templateParser;
	
	public TestRunner(String scenarioFilename, TemplateParser templateParser)
	{
		this.scenarioFilename = scenarioFilename;
		this.templateParser = templateParser;
	}

	@Override
	public void run()
	{
		try
		{
			ClassLoader classLoader = getClass().getClassLoader();
			File json = new File(classLoader.getResource(scenarioFilename).getFile());
			List<ScenarioRequest> requests = ScenarioRunner.parseRequests(json, templateParser);
			ScenarioRunner runner = new ScenarioRunner(requests);
			runner.start();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			latch.countDown();
		}
	}

	public void awaitFinished() throws InterruptedException
	{
		latch.await();
	}
}