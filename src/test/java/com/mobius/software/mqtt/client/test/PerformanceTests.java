package com.mobius.software.mqtt.client.test;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;

import com.mobius.software.mqtt.client.api.data.ClientReport;
import com.mobius.software.mqtt.client.api.data.CommandType;
import com.mobius.software.mqtt.client.api.data.Counter;
import com.mobius.software.mqtt.client.api.data.ErrorReport;
import com.mobius.software.mqtt.client.api.data.Property;
import com.mobius.software.mqtt.client.api.data.PropertyType;
import com.mobius.software.mqtt.client.api.json.GenericJsonResponse;
import com.mobius.software.mqtt.client.api.json.ResponseData;
import com.mobius.software.mqtt.client.api.json.ScenarioRequest;
import com.mobius.software.mqtt.client.api.json.UniqueIdentifierRequest;
import com.mobius.software.mqtt.client.controller.Report;
import com.mobius.software.mqtt.client.util.IdentifierParser;
import com.mobius.software.mqtt.client.util.PathSegment;
import com.mobius.software.mqtt.client.util.URLBuilder;

public class PerformanceTests
{
	private static final URI localBaseURL = URI.create("http://127.0.0.1:9998/");
	private static final URI remoteBaseURL = URI.create("http://52.174.105.139:9998/");

	@Test
	public void testIdentifierParser()
	{
		String regex = "%server%identity%";
		String username = "first@foo.bar";
		String server = "/192.168.0.100:1883";
		String startIdentifier = "1";
		int count = 10000;
		long start = System.currentTimeMillis();
		Set<String> set = new HashSet<>();
		for (int i = 0; i < count; i++)
			set.add(IdentifierParser.parseIdentifier(regex, username, server, startIdentifier));
		long stop = System.currentTimeMillis();
		System.out.println("time:" + (stop - start));
	}

	@Test
	public void testScript()
	{
		try
		{
			String filename = "local.json";
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource(filename).getFile());

			String url = URLBuilder.build(localBaseURL, PathSegment.CONTROLLER, PathSegment.SCENARIO);
			JSONContainer container = new JSONContainer(url);

			ScenarioRequest request = container.getMapper().readValue(file, ScenarioRequest.class);
			assertNotNull(request);
			assertTrue(request.validate());

			GenericJsonResponse response = container.post(request);
			assertEquals(response.getMessage(), ResponseData.SUCCESS, response.getStatus());

			Thread.sleep(90000);

			url = URLBuilder.build(localBaseURL, PathSegment.CONTROLLER, PathSegment.REPORT);
			container = new JSONContainer(url);
			UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(request.getRequests().get(0).getId());
			Report report = container.requestReport(reportRequest);
			assertNotNull(report);
			assertNotNull(report.getReports());

			if (!report.retrieveStatus())
				System.out.println("ERRORS OCCURED");

			if (request.getRequests().get(0).getCount() != report.getCompletedCount())
				System.out.println("Expected finished: " + request.getRequests().get(0).getCount() + ", actual: " + report.getCompletedCount());

			int failedClients = 0;
			for (ClientReport clientReport : report.getReports())
			{
				for (ErrorReport error : clientReport.getErrors())
					System.out.println(new Date(error.getTimestamp()) + "," + clientReport.getIdentifier() + "," + error.getType() + "," + error.getMessage());

				if (clientReport.getErrors().size() > 0)
					failedClients++;
				else
				{
					List<Counter> counters = clientReport.getCounters();
					if (!CounterComparator.compareByType(counters, CommandType.CONNECT))
					{
						System.out.println(clientReport.getIdentifier() + ":invalid connack count");
						failedClients++;
					}
					else if (!CounterComparator.compareByType(counters, CommandType.PINGREQ))
					{
						System.out.println(clientReport.getIdentifier() + ":invalid pingresp count");
						failedClients++;
					}
				}
			}

			url = URLBuilder.build(localBaseURL, PathSegment.CONTROLLER, PathSegment.CLEAR);
			container = new JSONContainer(url);
			UniqueIdentifierRequest clearRequest = new UniqueIdentifierRequest(request.getRequests().get(0).getId());
			GenericJsonResponse clearResponse = container.post(clearRequest);

			assertEquals(0, failedClients);
			assertEquals(request.getRequests().get(0).getCount(), report.getCompletedCount());
			assertEquals(clearResponse.getStatus(), "SUCCESS");
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

			String url = URLBuilder.build(remoteBaseURL, PathSegment.CONTROLLER, PathSegment.SCENARIO);
			JSONContainer container = new JSONContainer(url);

			ScenarioRequest request = container.getMapper().readValue(file, ScenarioRequest.class);
			assertNotNull(request);
			assertTrue(request.validate());

			GenericJsonResponse response = container.post(request);
			assertEquals(response.getMessage(), ResponseData.SUCCESS, response.getStatus());

			Thread.sleep(120000);

			url = URLBuilder.build(remoteBaseURL, PathSegment.CONTROLLER, PathSegment.REPORT);
			container = new JSONContainer(url);
			UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(request.getRequests().get(0).getId());
			Report report = container.requestReport(reportRequest);
			assertNotNull(report);
			assertNotNull(report.getReports());

			if (!report.retrieveStatus())
				System.out.println("ERRORS OCCURED");

			if (request.getRequests().get(0).getCount() != report.getCompletedCount())
				System.out.println("Expected finished: " + request.getRequests().get(0).getCount() + ", actual: " + report.getCompletedCount());

			int failedClients = 0;
			for (ClientReport clientReport : report.getReports())
			{
				for (ErrorReport error : clientReport.getErrors())
					System.out.println(new Date(error.getTimestamp()) + "," + clientReport.getIdentifier() + "," + error.getType() + "," + error.getMessage());

				if (clientReport.getErrors().size() > 0)
					failedClients++;
				else
				{
					List<Counter> counters = clientReport.getCounters();
					if (!CounterComparator.compareByType(counters, CommandType.CONNECT))
					{
						System.out.println(clientReport.getIdentifier() + ":invalid connack count");
						failedClients++;
					}
					else if (!CounterComparator.compareByType(counters, CommandType.PINGREQ))
					{
						System.out.println(clientReport.getIdentifier() + ":invalid pingresp count");
						failedClients++;
					}
				}
			}

			url = URLBuilder.build(remoteBaseURL, PathSegment.CONTROLLER, PathSegment.CLEAR);
			container = new JSONContainer(url);
			UniqueIdentifierRequest clearRequest = new UniqueIdentifierRequest(request.getRequests().get(0).getId());
			GenericJsonResponse clearResponse = container.post(clearRequest);

			assertEquals(0, failedClients);
			assertEquals(request.getRequests().get(0).getCount(), report.getCompletedCount());
			assertEquals(clearResponse.getStatus(), "SUCCESS");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testPingWithMultipleControllers()
	{
		try
		{
			String filename = "ping.json";
			ClassLoader classLoader = getClass().getClassLoader();
			File json = new File(classLoader.getResource(filename).getFile());

			URI firstControllerUri = URI.create("http://168.63.99.58:9998/");
			URI secondControllerUri = URI.create("http://168.63.98.228:9998/");
			URI thirdControllerUri = URI.create("http://137.116.205.185:9998/");
			//			URI fourthControllerUri = URI.create("http://13.95.233.20:9998/");

			ScenarioRequest request = sendScenarioRequest(firstControllerUri, json, "%account%identity%");
			sendScenarioRequest(secondControllerUri, json, "%server%identity%");
			sendScenarioRequest(thirdControllerUri, json, "%identity%account%");
			//			sendScenarioRequest(fourthControllerUri, json, "%identity%server%");

			UUID requestID = request.getRequests().get(0).getId();

			Thread.sleep(420000);

			List<Report> reports = new ArrayList<>();
			Report firstReport = retrieveScenarioReport(firstControllerUri, requestID);
			Report secondReport = retrieveScenarioReport(secondControllerUri, requestID);
			Report thirdReport = retrieveScenarioReport(thirdControllerUri, requestID);
			//			Report fourthReport = retrieveScenarioReport(fourthControllerUri, requestID);

			List<GenericJsonResponse> clearReports = new ArrayList<>();
			GenericJsonResponse firstClear = clear(firstControllerUri, requestID);
			GenericJsonResponse secondClear = clear(secondControllerUri, requestID);
			GenericJsonResponse thirdClear = clear(thirdControllerUri, requestID);
			//			GenericJsonResponse fourthClear = clear(fourthControllerUri, requestID);

			assertNotNull(firstReport);
			assertNotNull(firstReport.getReports());
			assertNotNull(secondReport);
			assertNotNull(secondReport.getReports());
			assertNotNull(thirdReport);
			assertNotNull(thirdReport.getReports());
			//			assertNotNull(fourthReport);
			//			assertNotNull(fourthReport.getReports());

			reports.add(firstReport);
			reports.add(secondReport);
			reports.add(thirdReport);
			//			reports.add(fourthReport);

			clearReports.add(firstClear);
			clearReports.add(secondClear);
			clearReports.add(thirdClear);
			//			clearReports.add(fourthClear);

			int count = 1;
			int failedClients = 0;
			int succesfullReports = 0;
			int succesfullClear = 0;
			int completedCount = 0;
			for (Report report : reports)
			{
				System.out.println("Report: " + count);
				count++;

				if (!report.retrieveStatus())
					System.out.println("ERRORS OCCURED");
				else
					succesfullReports++;

				if (request.getRequests().get(0).getCount() != report.getCompletedCount())
				{
					System.out.println("Expected finished: " + request.getRequests().get(0).getCount() + ", actual: " + report.getCompletedCount());
					completedCount += report.getCompletedCount();
				}

				for (ClientReport clientReport : report.getReports())
				{
					for (ErrorReport error : clientReport.getErrors())
						System.out.println(new Date(error.getTimestamp()) + "," + clientReport.getIdentifier() + "," + error.getType() + "," + error.getMessage());

					if (clientReport.getErrors().size() > 0)
						failedClients++;
					else
					{
						List<Counter> counters = clientReport.getCounters();
						if (!CounterComparator.compareByType(counters, CommandType.CONNECT))
						{
							System.out.println(clientReport.getIdentifier() + ":invalid connack count");
							failedClients++;
						}
						else if (!CounterComparator.compareByType(counters, CommandType.PINGREQ))
						{
							System.out.println(clientReport.getIdentifier() + ":invalid pingresp count");
							failedClients++;
						}
					}
				}
			}

			for (GenericJsonResponse response : clearReports)
			{
				if (!response.getStatus().equals("SUCCESS"))
					System.out.println("ERRORS OCCURED");
				else
					succesfullClear++;
			}

			assertEquals(0, failedClients);
			assertEquals(request.getRequests().get(0).getCount() * 3, completedCount);
			assertEquals(succesfullReports, 3);
			assertEquals(succesfullClear, 3);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	private ScenarioRequest sendScenarioRequest(URI controllerURI, File json, String identityRegex) throws Exception
	{
		String url = URLBuilder.build(controllerURI, PathSegment.CONTROLLER, PathSegment.SCENARIO);
		JSONContainer container = new JSONContainer(url);

		ScenarioRequest request = container.getMapper().readValue(json, ScenarioRequest.class);
		List<Property> connectProperty = request.getRequests().get(0).getCommands().get(0).getCommandProperties();
		connectProperty.set(0, new Property(PropertyType.IDENT_REGEX, identityRegex));
		assertNotNull(request);
		assertTrue(request.validate());

		GenericJsonResponse response = container.post(request);
		assertEquals(response.getMessage(), ResponseData.SUCCESS, response.getStatus());

		return request;
	}

	private Report retrieveScenarioReport(URI controllerURI, UUID scenarioID) throws Exception
	{
		String url = URLBuilder.build(controllerURI, PathSegment.CONTROLLER, PathSegment.REPORT);
		JSONContainer container = new JSONContainer(url);
		UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(scenarioID);
		return container.requestReport(reportRequest);
	}

	private GenericJsonResponse clear(URI controllerURI, UUID scenarioID) throws Exception
	{
		String url = URLBuilder.build(controllerURI, PathSegment.CONTROLLER, PathSegment.CLEAR);
		JSONContainer container = new JSONContainer(url);
		UniqueIdentifierRequest clearRequest = new UniqueIdentifierRequest(scenarioID);
		return container.post(clearRequest);
	}
}
