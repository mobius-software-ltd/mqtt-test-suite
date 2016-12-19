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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobius.software.mqtt.performance.api.data.ClientController;
import com.mobius.software.mqtt.performance.api.json.MultiScenarioData;
import com.mobius.software.mqtt.performance.api.json.ScenarioRequest;
import com.mobius.software.mqtt.performance.commons.data.PathSegment;
import com.mobius.software.mqtt.performance.commons.util.URLBuilder;

public class RequestFormatter
{
	public static List<ScenarioRequest> parseScenarioRequests(File json) throws JsonParseException, JsonMappingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		MultiScenarioData multiScenarioData = mapper.readValue(json, MultiScenarioData.class);
		if (!multiScenarioData.validate())
			throw new BadRequestException("JSON file: one of the required fields is missing or invalid");

		List<ScenarioRequest> requests = new ArrayList<>();
		for (ClientController controller : multiScenarioData.getControllers())
		{
			ScenarioRequest request = mapper.readValue(json, MultiScenarioData.class).getScenarioRequest().fillData(controller);
			String requestUrl = URLBuilder.build(controller.getHostname(), controller.getPort(), PathSegment.CONTROLLER, PathSegment.SCENARIO);
			request.updateURL(requestUrl);
			requests.add(request);
		}
		return requests;
	}

	public static long parseRequestTimeout(File json) throws JsonParseException, JsonMappingException, IOException
	{
		ObjectMapper mapper = new ObjectMapper();
		MultiScenarioData multiScenarioData = mapper.readValue(json, MultiScenarioData.class);
		if (!multiScenarioData.validate())
			throw new BadRequestException("JSON file: one of the required fields is missing or invalid");

		return multiScenarioData.getRequestTimeout();
	}
}
