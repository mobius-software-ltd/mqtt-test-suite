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

package com.mobius.software.mqtt.performance.runner.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.performance.api.json.GenericJsonRequest;
import com.mobius.software.mqtt.performance.api.json.ReportResponse;
import com.mobius.software.mqtt.performance.api.json.ScenarioRequest;
import com.mobius.software.mqtt.performance.api.json.UniqueIdentifierRequest;
import com.mobius.software.mqtt.performance.commons.data.PathSegment;
import com.mobius.software.mqtt.performance.commons.util.URLBuilder;
import com.mobius.software.mqtt.performance.runner.JSONContainer;
import com.mobius.software.mqtt.performance.runner.ScenarioData;

public class Requester
{
	private static final Log logger = LogFactory.getLog(Requester.class);

	public static List<ScenarioData> submitScenarios(ScenarioRequest request)
	{
		List<ScenarioData> list = new ArrayList<>();
		JSONContainer container = new JSONContainer(request.retrieveURL());
		GenericJsonRequest response = null;
		try
		{
			response = container.post(request);
		}
		catch (Exception e)
		{
			logger.error("AN ERROR OCCURED WHILE SENDING SCENARIO REQUEST:" + e.getMessage());
			return list;
		}
		finally
		{
			container.release();
		}
		if (response.successful())
			list.addAll(ScenarioData.translateAll(request));
		else
			logger.error("An error occured while submiting scenarios:" + response.getMessage());
		return list;
	}

	public static ReportResponse requestReport(ScenarioData data)
	{
		String url = URLBuilder.build(data.getBaseURI(), PathSegment.CONTROLLER, PathSegment.REPORT);
		UniqueIdentifierRequest request = new UniqueIdentifierRequest(data.getScenarioID());
		JSONContainer container = new JSONContainer(url);
		ReportResponse report = null;
		try
		{
			report = container.requestReport(request);
		}
		catch (Exception e)
		{
			logger.error("SCENARIO REPORT REQUEST FAILED:" + report.getMessage());
			return report;
		}
		finally
		{
			container.release();
		}
		return report;
	}

	public static void requestClear(ScenarioData data)
	{
		String clearURL = URLBuilder.build(data.getBaseURI(), PathSegment.CONTROLLER, PathSegment.CLEAR);
		UniqueIdentifierRequest request = new UniqueIdentifierRequest(data.getScenarioID());
		JSONContainer container = new JSONContainer(clearURL);
		GenericJsonRequest response = null;
		try
		{
			response = container.post(request);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("SCENARIO CLEAR REQUEST FAILED:" + response.getMessage());
		}
		finally
		{
			container.release();
		}
		if (!response.successful())
			logger.error("An error occured while clearing scenario:" + response.getMessage());
	}
}
