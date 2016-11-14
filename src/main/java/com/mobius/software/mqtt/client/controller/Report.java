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

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.mobius.software.mqtt.client.api.data.ClientReport;
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

	public Report(String status, Long messagesSent, Long messagesReceived, List<ClientReport> reports, Integer completedCount)
	{
		super(status, null);
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
