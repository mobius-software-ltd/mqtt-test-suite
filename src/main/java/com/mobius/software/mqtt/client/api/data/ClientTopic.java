package com.mobius.software.mqtt.client.api.data;

import com.mobius.software.mqtt.parser.QoS;
import com.mobius.software.mqtt.parser.Text;
import com.mobius.software.mqtt.parser.Topic;

public class ClientTopic
{
	private String name;
	private Integer qos;

	public ClientTopic()
	{

	}

	public ClientTopic(String name, Integer qos)
	{
		this.name = name;
		this.qos = qos;
	}

	public Topic toTopic()
	{
		return new Topic(new Text(name), QoS.valueOf(qos));
	}

	public static ClientTopic valueOf(Topic topic)
	{
		return new ClientTopic(topic.getName().toString(), topic.getQos().getValue());
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Integer getQos()
	{
		return qos;
	}

	public void setQos(Integer qos)
	{
		this.qos = qos;
	}

	public boolean validate()
	{
		return name != null && qos != null && (qos == 0 || qos == 1 || qos == 2);
	}
}
