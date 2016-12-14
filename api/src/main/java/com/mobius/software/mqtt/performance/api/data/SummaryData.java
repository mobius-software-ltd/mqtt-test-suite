package com.mobius.software.mqtt.performance.api.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.mobius.software.mqtt.performance.api.json.UniqueIdentifierRequest;
import com.mobius.software.mqtt.performance.commons.data.CommandCounter;
import com.mobius.software.mqtt.performance.commons.data.CommandType;
import com.mobius.software.mqtt.performance.commons.data.Counter;
import com.mobius.software.mqtt.performance.commons.data.Direction;
import com.mobius.software.mqtt.performance.commons.data.PathSegment;
import com.mobius.software.mqtt.performance.commons.util.CommandParser;
import com.mobius.software.mqtt.performance.commons.util.URLBuilder;

public class SummaryData
{
	private String baseURI;
	private UUID scenarioID;
	private Boolean status;
	private long startTime;
	private long finishTime;
	private int totalClients;
	private int totalCommands;
	private int finishedClients;
	private int failedClients;
	private int finishedCommands;
	private int failedCommands;
	private int totalErrors = 0;
	private Map<CommandType, AtomicInteger> incomingCounters = new LinkedHashMap<>();
	private Map<CommandType, AtomicInteger> outgoingCounters = new LinkedHashMap<>();
	private Counter duplicatesIn = new Counter(0, Direction.INCOMING);
	private Counter duplicatesOut = new Counter(0, Direction.OUTGOING);

	public SummaryData(String baseURI, UUID scenarioID, int totalClients, int totalCommands)
	{
		this.baseURI = baseURI;
		this.scenarioID = scenarioID;
		this.totalClients = totalClients;
		this.totalCommands = totalCommands;
		for (CommandType type : CommandType.values())
		{
			incomingCounters.put(type, new AtomicInteger(0));
			outgoingCounters.put(type, new AtomicInteger(0));
		}
	}

	public static SummaryData initFrom(String baseURI, Scenario scenario)
	{
		UUID scenarioID = scenario.getId();
		int totalClients = scenario.getCount();
		int totalCommands = totalClients * CommandParser.retrieveCommands(scenario.getCommands()).size();
		return new SummaryData(baseURI, scenarioID, totalClients, totalCommands);
	}

	public void parseResult(ReportResponse report)
	{
		this.startTime = report.getStartTime();
		this.finishTime = report.getFinishTime();
		this.status = true;
		this.finishedClients = this.totalClients;
		this.finishedCommands = this.totalCommands;
		this.failedClients = 0;
		this.failedCommands = 0;

		List<ClientReport> clientReports = report.getReports();
		for (ClientReport clientReport : clientReports)
		{
			boolean clientSuccessStatus = true;
			int unfinishedCommands = clientReport.getUnfinishedCommands();
			if (unfinishedCommands > 0)
			{
				clientSuccessStatus = false;
				this.finishedCommands -= unfinishedCommands;
				this.failedCommands += unfinishedCommands;
			}

			if (!clientReport.getErrors().isEmpty())
			{
				totalErrors += clientReport.getErrors().size();
				clientSuccessStatus = false;
			}

			for (CommandCounter counter : clientReport.getCommandCounters())
			{
				switch (counter.getDirection())
				{
				case INCOMING:
					incomingCounters.get(counter.getCommand()).getAndAdd(counter.getCount());
					break;

				case OUTGOING:
					outgoingCounters.get(counter.getCommand()).getAndAdd(counter.getCount());
					break;
				}
			}

			for (Counter counter : clientReport.getDuplicateCounters())
			{
				if (counter.getDirection() == Direction.INCOMING)
					duplicatesIn.setCount(duplicatesIn.getCount() + counter.getCount());
				else
					duplicatesOut.setCount(duplicatesOut.getCount() + counter.getCount());
			}

			if (!clientSuccessStatus)
			{
				this.status = false;
				this.finishedClients -= 1;
				this.failedClients += 1;
			}
		}
	}

	public ControllerRequest arrangeReportRequest()
	{
		String url = URLBuilder.build(baseURI, PathSegment.CONTROLLER, PathSegment.REPORT);
		UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(scenarioID);
		ControllerRequest request = new ControllerRequest(url, reportRequest);
		return request;
	}

	public ControllerRequest arrangeClearRequest()
	{
		String url = URLBuilder.build(baseURI, PathSegment.CONTROLLER, PathSegment.CLEAR);
		UniqueIdentifierRequest reportRequest = new UniqueIdentifierRequest(scenarioID);
		ControllerRequest request = new ControllerRequest(url, reportRequest);
		return request;
	}

	public String getBaseURI()
	{
		return baseURI;
	}

	public void setBaseURI(String baseURI)
	{
		this.baseURI = baseURI;
	}

	public UUID getScenarioID()
	{
		return scenarioID;
	}

	public void setScenarioID(UUID scenarioID)
	{
		this.scenarioID = scenarioID;
	}

	public boolean getStatus()
	{
		return status;
	}

	public void setStatus(Boolean status)
	{
		this.status = status;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getFinishTime()
	{
		return finishTime;
	}

	public void setFinishTime(long finishTime)
	{
		this.finishTime = finishTime;
	}

	public int getTotalClients()
	{
		return totalClients;
	}

	public void setTotalClients(int totalClients)
	{
		this.totalClients = totalClients;
	}

	public int getTotalCommands()
	{
		return totalCommands;
	}

	public void setTotalCommands(int totalCommands)
	{
		this.totalCommands = totalCommands;
	}

	public int getFinishedClients()
	{
		return finishedClients;
	}

	public void setFinishedClients(int finishedClients)
	{
		this.finishedClients = finishedClients;
	}

	public int getFailedClients()
	{
		return failedClients;
	}

	public void setFailedClients(int failedClients)
	{
		this.failedClients = failedClients;
	}

	public int getFinishedCommands()
	{
		return finishedCommands;
	}

	public void setFinishedCommands(int finishedCommands)
	{
		this.finishedCommands = finishedCommands;
	}

	public int getFailedCommands()
	{
		return failedCommands;
	}

	public void setFailedCommands(int failedCommands)
	{
		this.failedCommands = failedCommands;
	}

	public Map<CommandType, AtomicInteger> getIncomingCounters()
	{
		return incomingCounters;
	}

	public void setIncomingCounters(Map<CommandType, AtomicInteger> incomingCounters)
	{
		this.incomingCounters = incomingCounters;
	}

	public Map<CommandType, AtomicInteger> getOutgoingCounters()
	{
		return outgoingCounters;
	}

	public void setOutgoingCounters(Map<CommandType, AtomicInteger> outgoingCounters)
	{
		this.outgoingCounters = outgoingCounters;
	}

	public Counter getDuplicatesIn()
	{
		return duplicatesIn;
	}

	public void setDuplicatesIn(Counter duplicatesIn)
	{
		this.duplicatesIn = duplicatesIn;
	}

	public Counter getDuplicatesOut()
	{
		return duplicatesOut;
	}

	public void setDuplicatesOut(Counter duplicatesOut)
	{
		this.duplicatesOut = duplicatesOut;
	}

	public int getTotalErrors()
	{
		return totalErrors;
	}

	public void setTotalErrors(int totalErrors)
	{
		this.totalErrors = totalErrors;
	}
}
