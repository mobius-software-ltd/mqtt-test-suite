package com.mobius.software.mqtt.client.api.json;

import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@XmlRootElement
public class ObjectResponse<T> extends GenericJsonResponse
{
	private T data;

	public ObjectResponse()
	{
		super();
	}

	public ObjectResponse(T data)
	{
		super(ResponseData.SUCCESS, null);
		this.data = data;
	}

	public T getData()
	{
		return data;
	}

	public void setData(T data)
	{
		this.data = data;
	}
}