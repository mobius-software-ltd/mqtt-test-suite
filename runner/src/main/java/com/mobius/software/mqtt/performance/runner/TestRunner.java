package com.mobius.software.mqtt.performance.runner;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.BadRequestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.mqtt.performance.api.data.ClientController;
import com.mobius.software.mqtt.performance.api.data.ClientReport;
import com.mobius.software.mqtt.performance.api.data.ControllerRequest;
import com.mobius.software.mqtt.performance.api.data.ReportResponse;
import com.mobius.software.mqtt.performance.api.data.Scenario;
import com.mobius.software.mqtt.performance.api.data.SummaryData;
import com.mobius.software.mqtt.performance.api.json.GenericJsonResponse;
import com.mobius.software.mqtt.performance.api.json.MultiScenarioRequest;
import com.mobius.software.mqtt.performance.api.json.ResponseData;
import com.mobius.software.mqtt.performance.api.json.ScenarioRequest;
import com.mobius.software.mqtt.performance.commons.data.PathSegment;
import com.mobius.software.mqtt.performance.commons.util.URLBuilder;
import com.mobius.software.mqtt.performance.runner.util.FileUtil;
import com.mobius.software.mqtt.performance.runner.util.ReportBuilder;
import com.mobius.software.mqtt.performance.runner.util.ReportWriter;

public class TestRunner
{
	private static final Log logger = LogFactory.getLog(TestRunner.class);

	public static void main(String[] args)
	{
		try
		{
			File json = FileUtil.readJSONFile(args[0]);

			ObjectMapper mapper = new ObjectMapper();
			MultiScenarioRequest controllersScenarioRequests = mapper.readValue(json, MultiScenarioRequest.class);
			if (!controllersScenarioRequests.validate())
				throw new BadRequestException("JSON file: one of the required fields is missing or invalid");

			Map<UUID, SummaryData> summaryData = new HashMap<>();
			for (ClientController controller : controllersScenarioRequests.getControllers())
			{
				String baseURI = URLBuilder.buildBaseURI(controller.getHostname(), controller.getPort());
				List<Scenario> scenarios = controller.translateScenarioRequests(mapper.readValue(json, MultiScenarioRequest.class).getScenarioRequest());
				for (Scenario scenario : scenarios)
				{
					SummaryData data = SummaryData.initFrom(baseURI, scenario);
					summaryData.put(scenario.getId(), data);
				}

				String url = URLBuilder.build(baseURI, PathSegment.CONTROLLER, PathSegment.SCENARIO);
				ScenarioRequest scenarioRequest = new ScenarioRequest();
				scenarioRequest.setRequests(scenarios);
				ControllerRequest request = new ControllerRequest(url, scenarioRequest);
				requestScenario(request);
			}

			if (summaryData.isEmpty())
				throw new IllegalStateException("ALL SCENARIO REQUESTS FAILED!");

			Thread.sleep(controllersScenarioRequests.getRequestTimeout());

			for (SummaryData data : summaryData.values())
			{
				ReportResponse reportResponse = requestReport(data.arrangeReportRequest());
				if (reportResponse != null)
				{
					if (reportResponse.successful())
					{
						data.parseResult(reportResponse);
						String summary = ReportBuilder.buildSummary(data);
						System.out.println(summary);

						List<ClientReport> reports = reportResponse.getReports();
						try
						{
							if (!reports.isEmpty())
								ReportWriter.writeErrors(data.getScenarioID(), reports);
						}
						catch (IOException e)
						{
							logger.error("An error occured while writing error reports to file");
						}

						requestClear(data.arrangeClearRequest());
					}
					else
						logger.error("An error occured while retrieving scenario report:" + reportResponse.getMessage());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error(e.getMessage());
			System.exit(0);
		}
	}

	private static boolean requestScenario(ControllerRequest request)
	{
		JSONContainer container = new JSONContainer(request.getUrl());
		GenericJsonResponse response = null;
		try
		{
			response = container.post(request.getRequest());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("AN ERROR OCCURED WHILE SENDING SCENARIO REQUEST:" + e.getMessage());
			return false;
		}
		finally
		{
			container.release();
		}
		return response.getStatus() == ResponseData.SUCCESS;
	}

	private static ReportResponse requestReport(ControllerRequest request)
	{
		ReportResponse report = null;
		JSONContainer container = new JSONContainer(request.getUrl());
		try
		{
			report = container.requestReport(request.getRequest());
		}
		catch (Exception e)
		{
			logger.error("SCENARIO REPORT REQUEST FAILED:" + report.getMessage());
			return null;
		}
		finally
		{
			container.release();
		}
		return report;
	}

	private static boolean requestClear(ControllerRequest request)
	{
		JSONContainer container = new JSONContainer(request.getUrl());
		GenericJsonResponse response = null;
		try
		{
			response = container.post(request.getRequest());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("SCENARIO CLEAR REQUEST FAILED:" + response.getMessage());
		}
		container.release();
		return response.getStatus() == ResponseData.SUCCESS;
	}
}
