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

package com.mobius.software.mqtt.performance.runner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.performance.api.json.ReportResponse;
import com.mobius.software.mqtt.performance.api.json.ScenarioRequest;
import com.mobius.software.mqtt.performance.runner.util.FileUtil;
import com.mobius.software.mqtt.performance.runner.util.ReportBuilder;
import com.mobius.software.mqtt.performance.runner.util.RequestFormatter;
import com.mobius.software.mqtt.performance.runner.util.Requester;

public class TestRunner
{
	private static final Log logger = LogFactory.getLog(TestRunner.class);

	public static void main(String[] args)
	{
		try
		{
			File json = FileUtil.readFile(args[0]);

			List<ScenarioData> scenarioData = new ArrayList<>();
			List<ScenarioRequest> requests = RequestFormatter.parseScenarioRequests(json);
			for (ScenarioRequest request : requests)
			{
				List<ScenarioData> data = Requester.submitScenarios(request);
				scenarioData.addAll(data);
			}
			if (scenarioData.isEmpty())
				throw new IllegalStateException("ALL SCENARIO REQUESTS FAILED!");

			Thread.sleep(RequestFormatter.parseRequestTimeout(json));

			for (ScenarioData data : scenarioData)
			{
				ReportResponse report = Requester.requestReport(data);
				if (report != null && report.successful())
				{
					String summary = ReportBuilder.buildSummary(data.parseReport(report));
					System.out.println(summary);

					FileUtil.logErrors(data.getScenarioID(), report.getReports());
					Requester.requestClear(data);
				}
				else
					logger.error("An error occured while retrieving scenario report:" + report.getMessage());
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			System.exit(0);
		}
	}
}
