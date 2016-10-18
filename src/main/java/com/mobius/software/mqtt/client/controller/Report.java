package com.mobius.software.mqtt.client.controller;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.mobius.software.mqtt.client.ClientReport;
import com.mobius.software.mqtt.client.api.json.GenericJsonResponse;

@SuppressWarnings("serial")
@XmlRootElement
public class Report extends GenericJsonResponse
{
	private Integer completedCount;
	private Long messagesSent;
	private Long messagesReceived;
	private List<ClientReport> reports;

	public Report()
	{

	}

	public Report(String status, String message, Long messagesSent, Long messagesReceived, List<ClientReport> reports, Integer completedCount)
	{
		super(status, message);
		this.messagesSent = messagesSent;
		this.messagesReceived = messagesReceived;
		this.reports = reports;
		this.completedCount = completedCount;
	}

	public Boolean retrieveStatus()
	{
		if (reports != null)
		{
			for (ClientReport report : reports)
			{
				if (report.getErrors() != null && !report.getErrors().isEmpty())
					return false;
			}
		}
		return true;
	}

	public Integer getCompletedCount()
	{
		return completedCount;
	}

	public void setCompletedCount(Integer completedCount)
	{
		this.completedCount = completedCount;
	}

	public Long getMessagesSent()
	{
		return messagesSent;
	}

	public void setMessagesSent(Long messagesSent)
	{
		this.messagesSent = messagesSent;
	}

	public Long getMessagesReceived()
	{
		return messagesReceived;
	}

	public void setMessagesReceived(Long messagesReceived)
	{
		this.messagesReceived = messagesReceived;
	}

	public List<ClientReport> getReports()
	{
		return reports;
	}

	public void setReports(List<ClientReport> reports)
	{
		this.reports = reports;
	}
}
