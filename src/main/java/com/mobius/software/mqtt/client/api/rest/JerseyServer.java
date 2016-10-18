package com.mobius.software.mqtt.client.api.rest;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyServer
{
	private HttpServer server;
	
	public JerseyServer(URI uri)
	{
		final ResourceConfig rc = new ResourceConfig().packages("com.mobius.software.mqtt.client.controller");
		server = GrizzlyHttpServerFactory.createHttpServer(uri, rc);		
	}    
	
	public void terminate()
	{
		server.shutdown();
	}
}
