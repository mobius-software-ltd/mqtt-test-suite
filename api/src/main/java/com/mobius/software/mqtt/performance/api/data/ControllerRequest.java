package com.mobius.software.mqtt.performance.api.data;


public class ControllerRequest
{
	private final String url;
	private final Object request;

	public ControllerRequest(String url, Object request)
	{
		this.url = url;
		this.request = request;
	}

	public String getUrl()
	{
		return url;
	}

	public Object getRequest()
	{
		return request;
	}
}
