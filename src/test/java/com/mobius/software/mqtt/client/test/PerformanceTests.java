package com.mobius.software.mqtt.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.mqtt.client.ClientReport;
import com.mobius.software.mqtt.client.Counter;
import com.mobius.software.mqtt.client.ErrorReport;
import com.mobius.software.mqtt.client.api.data.CommandType;
import com.mobius.software.mqtt.client.api.json.GenericJsonResponse;
import com.mobius.software.mqtt.client.api.json.ResponseData;
import com.mobius.software.mqtt.client.api.json.ScenarioRequest;
import com.mobius.software.mqtt.client.api.json.UniqueIdentifierRequest;
import com.mobius.software.mqtt.client.controller.Report;
import com.mobius.software.mqtt.client.util.PathSegment;
import com.mobius.software.mqtt.client.util.URLBuilder;

public class PerformanceTests
{
	private static final URI baseURL = URI.create("http://127.0.0.1:9998/");
	private static Logger logger = Logger.getLogger(PerformanceTests.class);

	@Test
	public void testScript()
	{
		try
		{
			String filename = "local.json";
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource(filename).getFile());

			ObjectMapper mapper = new ObjectMapper();
			ScenarioRequest request = mapper.readValue(file, ScenarioRequest.class);
			assertNotNull(request);
			assertTrue(request.validate());

			String url = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.SCENARIO);
			JSONContainer container = new JSONContainer(url);
			GenericJsonResponse response = container.post(request);
			assertEquals(response.getMessage(), ResponseData.SUCCESS, response.getStatus());

			Thread.sleep(30000);

			url = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.REPORT);
			container = new JSONContainer(url);
			UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(request.getRequests().get(0).getId());
			Report report = container.requestReport(reportRequest);
			assertNotNull(report);
			assertNotNull(report.getReports());

			if (!report.retrieveStatus())
				logger.error("ERRORS OCCURED");

			if (request.getRequests().get(0).getCount() != report.getCompletedCount())
				logger.error("Expected finished: " + request.getRequests().get(0).getCount() + ", actual: " + report.getCompletedCount());

			int failedClients = 0;
			for (ClientReport clientReport : report.getReports())
			{
				for (ErrorReport error : clientReport.getErrors())
					logger.error(new Date(error.getTimestamp()) + "," + clientReport.getIdentifier() + "," + error.getType() + "," + error.getMessage());

				if (clientReport.getErrors().size() > 0)
					failedClients++;
				else
				{
					List<Counter> counters = clientReport.getCounters();
					if (!CounterComparator.compareByType(counters, CommandType.CONNECT))
					{
						logger.error(clientReport.getIdentifier() + ":invalid connack count");
						failedClients++;
					}
					else if (!CounterComparator.compareByType(counters, CommandType.PINGREQ))
					{
						logger.error(clientReport.getIdentifier() + ":invalid pingresp count");
						failedClients++;
					}
				}
			}

			assertEquals(0, failedClients);
			assertEquals(request.getRequests().get(0).getCount(), report.getCompletedCount());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPing()
	{
		try
		{
			String filename = "ping.json";
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource(filename).getFile());

			ObjectMapper mapper = new ObjectMapper();
			ScenarioRequest request = mapper.readValue(file, ScenarioRequest.class);
			assertNotNull(request);
			assertTrue(request.validate());

			String url = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.SCENARIO);
			JSONContainer container = new JSONContainer(url);
			GenericJsonResponse response = container.post(request);
			assertEquals(response.getMessage(), ResponseData.SUCCESS, response.getStatus());

			Thread.sleep(100000);

			url = URLBuilder.build(baseURL, PathSegment.CONTROLLER, PathSegment.REPORT);
			container = new JSONContainer(url);
			UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(request.getRequests().get(0).getId());
			Report report = container.requestReport(reportRequest);
			assertNotNull(report);
			assertNotNull(report.getReports());

			if (!report.retrieveStatus())
				logger.error("ERRORS OCCURED");

			if (request.getRequests().get(0).getCount() != report.getCompletedCount())
				logger.error("Expected finished: " + request.getRequests().get(0).getCount() + ", actual: " + report.getCompletedCount());

			int failedClients = 0;
			for (ClientReport clientReport : report.getReports())
			{
				for (ErrorReport error : clientReport.getErrors())
					logger.error(new Date(error.getTimestamp()) + "," + clientReport.getIdentifier() + "," + error.getType() + "," + error.getMessage());

				if (clientReport.getErrors().size() > 0)
					failedClients++;
				else
				{
					List<Counter> counters = clientReport.getCounters();
					if (!CounterComparator.compareByType(counters, CommandType.CONNECT))
					{
						logger.error(clientReport.getIdentifier() + ":invalid connack count");
						failedClients++;
					}
					else if (!CounterComparator.compareByType(counters, CommandType.PINGREQ))
					{
						logger.error(clientReport.getIdentifier() + ":invalid pingresp count");
						failedClients++;
					}
				}
			}

			assertEquals(0, failedClients);
			assertEquals(request.getRequests().get(0).getCount(), report.getCompletedCount());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}
}
