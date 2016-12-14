package com.mobius.software.com.mqtt.performance;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.mqtt.performance.api.data.ClientReport;
import com.mobius.software.mqtt.performance.api.data.ErrorReport;
import com.mobius.software.mqtt.performance.api.data.ErrorReportComparator;
import com.mobius.software.mqtt.performance.api.data.ErrorType;
import com.mobius.software.mqtt.performance.api.data.ReportResponse;
import com.mobius.software.mqtt.performance.api.data.SummaryData;
import com.mobius.software.mqtt.performance.api.json.MultiScenarioRequest;
import com.mobius.software.mqtt.performance.api.json.ResponseData;
import com.mobius.software.mqtt.performance.commons.data.CommandCounter;
import com.mobius.software.mqtt.performance.commons.data.CommandType;
import com.mobius.software.mqtt.performance.commons.data.Counter;
import com.mobius.software.mqtt.performance.commons.data.Direction;
import com.mobius.software.mqtt.performance.runner.util.ReportBuilder;

public class JsonParseTests
{
	private static final String filePath = "/home/anatolysatanovskiy/mqtt/performance_runner/pipeline.json";

	@Test
	public void testSout()
	{
		try
		{
			for (int k = 0; k < 2; k++)
			{
				UUID scenarioID = UUID.randomUUID();
				Calendar start = Calendar.getInstance();
				start.set(2016, 11, 8, 15, 25, 12);
				Calendar finish = Calendar.getInstance();
				finish.set(2016, 11, 8, 15, 31, 47);

				long startTime = start.getTime().getTime();
				long finishTime = finish.getTime().getTime();

				int totalClients = 5;
				int totalCommands = 10;
				SummaryData data = new SummaryData("http://localhost/controller/scenario", scenarioID, totalClients, totalCommands);
				List<ClientReport> reports = new ArrayList<ClientReport>();
				for (int i = 0; i < totalClients; i++)
				{
					String identifier = UUID.randomUUID().toString();
					int unfinishedCommands = new Random().nextInt(totalCommands / totalClients);
					List<Counter> duplicateCountes = new ArrayList<>();
					for (int j = 0; j < 5; j++)
					{
						Direction direction = j % 2 == 1 ? Direction.INCOMING : Direction.OUTGOING;
						Counter counter = new Counter(j, direction);
						duplicateCountes.add(counter);
					}
					List<CommandCounter> commandCounters = new ArrayList<>(totalCommands / totalClients);
					for (int j = 0; j < totalCommands / totalClients; j++)
					{
						int randomType = new Random().nextInt(14) + 1;
						CommandType type = CommandType.fromInt(randomType);
						Direction direction = j % 2 == 1 ? Direction.INCOMING : Direction.OUTGOING;
						CommandCounter counter = new CommandCounter(type, 1, direction);
						commandCounters.add(counter);
					}

					List<ErrorReport> errors = new ArrayList<ErrorReport>();
					int errorCount = new Random().nextInt(10) + 1;
					for (int j = 0; j < errorCount; j++)
					{
						ErrorType type = ErrorType.fromInt(new Random().nextInt(14));

						long timestamp = new Random().nextInt((int) (finishTime - startTime)) + startTime;
						ErrorReport error = new ErrorReport(type, "generic message for ", timestamp);
						errors.add(error);
					}
					ClientReport clientReport = new ClientReport(identifier, unfinishedCommands, commandCounters, duplicateCountes, errors);
					reports.add(clientReport);
				}
				ReportResponse report = new ReportResponse(ResponseData.ERROR, scenarioID, startTime, finishTime, reports);
				data.parseResult(report);

				String summary = ReportBuilder.buildSummary(data);
				System.out.println(summary);

				for (ClientReport clientReport : report.getReports())
				{
					List<ErrorReport> errorReports = clientReport.getErrors();
					Collections.sort(errorReports, new ErrorReportComparator());
					String errorContent = ReportBuilder.buildError(clientReport.getIdentifier(), errorReports);
					System.out.println(errorContent);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testLength()
	{
		try
		{
			System.out.println(new String(System.currentTimeMillis() + "").length());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testParseFile()
	{
		File json = new File(filePath);
		if (!json.exists())
		{
			System.out.println(json.getAbsolutePath() + " file not found!");
			fail();
		}

		ObjectMapper mapper = new ObjectMapper();
		MultiScenarioRequest controllersScenarioRequests = null;
		try
		{
			controllersScenarioRequests = mapper.readValue(json, MultiScenarioRequest.class);
		}
		catch (IOException e)
		{
			System.out.println("an error occured while reading " + json.getName() + " file:" + e.getMessage());
			fail();
		}
		if (!controllersScenarioRequests.validate())
		{
			System.out.println("an error occured while parsing " + json.getName());
			fail();
		}
	}
}