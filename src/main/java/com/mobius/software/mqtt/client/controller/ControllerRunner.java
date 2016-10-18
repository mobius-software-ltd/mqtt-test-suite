package com.mobius.software.mqtt.client.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.mobius.software.mqtt.client.api.rest.JerseyServer;

public class ControllerRunner
{
	private static final Logger logger = Logger.getLogger(ControllerRunner.class);

	public static String configFile=null;
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);

		try
		{
			URI baseURI = URI.create(args[0].replace("-baseURI=", ""));
			configFile=args[1].replace("-configFile=", "");
			
			JerseyServer server = new JerseyServer(baseURI);			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("press any key to stop: ");
			br.readLine();
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
