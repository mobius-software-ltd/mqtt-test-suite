package com.mobius.software.mqtt.client.test;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.mobius.software.mqtt.client.api.json.GenericJsonResponse;
import com.mobius.software.mqtt.client.controller.Report;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class JSONContainer
{
	private ClientConfig config;
	private Client client;
	private WebResource target;
	private ObjectMapper mapper;
	private int usageCount = 0;

	public JSONContainer(String url)
	{
		config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		client = Client.create(config);
		target = client.resource(url);
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public void updateURL(String url)
	{
		config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		client = Client.create(config);
		target = client.resource(url);
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public GenericJsonResponse post(Object request) throws Exception
	{
		Builder builder = getResource().getRequestBuilder().type(MediaType.APPLICATION_JSON);
		builder = builder.accept(MediaType.APPLICATION_JSON);
		return getMapper().readValue(builder.post(String.class, request), new TypeReference<GenericJsonResponse>()
		{
		});
	}

	public Report requestReport(Object request) throws Exception
	{
		Builder builder = getResource().getRequestBuilder().type(MediaType.APPLICATION_JSON);
		builder = builder.accept(MediaType.APPLICATION_JSON);
		return getMapper().readValue(builder.post(String.class, request), new TypeReference<Report>()
		{
		});
	}

	public GenericJsonResponse get() throws Exception
	{
		Builder builder = getResource().getRequestBuilder().type(MediaType.APPLICATION_JSON);
		builder = builder.accept(MediaType.APPLICATION_JSON);
		getMapper();
		GenericJsonResponse response = getMapper().readValue(builder.get(String.class), new TypeReference<GenericJsonResponse>()
		{
		});
		return response;
	}

	public WebResource getResource()
	{
		return target;
	}

	public ObjectMapper getMapper()
	{
		return mapper;
	}

	public void increaseUsageCount()
	{
		usageCount++;
	}

	public int getUsageCount()
	{
		return usageCount;
	}

	public void release()
	{
		client.destroy();
	}
}