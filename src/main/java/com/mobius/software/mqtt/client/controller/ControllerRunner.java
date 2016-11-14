package com.mobius.software.mqtt.client.controller;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mobius.software.mqtt.client.api.rest.JerseyServer;

public class ControllerRunner
{
	private static final Log logger = LogFactory.getLog(ControllerRunner.class);

	public static String configFile;

	public static void main(String[] args)
	{
		try
		{
			URI baseURI = URI.create(args[0].replace("-baseURI=", ""));
			configFile = args[1].replace("-configFile=", "");
			JerseyServer server = new JerseyServer(baseURI);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("press any key to stop: ");
			br.readLine();
			server.terminate();
		}
		catch (Exception e)
		{
			logger.error("AN ERROR OCCURED: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			System.exit(0);
		}
	}
}
