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

package com.mobius.software.mqtt.performance.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mobius.software.mqtt.performance.api.data.Scenario;
import com.mobius.software.mqtt.performance.api.json.GenericJsonRequest;
import com.mobius.software.mqtt.performance.api.json.ReportResponse;
import com.mobius.software.mqtt.performance.api.json.ResponseData;
import com.mobius.software.mqtt.performance.api.json.ScenarioRequest;
import com.mobius.software.mqtt.performance.api.json.UniqueIdentifierRequest;
import com.mobius.software.mqtt.performance.commons.util.CommandParser;
import com.mobius.software.mqtt.performance.commons.util.IdentifierParser;
import com.mobius.software.mqtt.performance.controller.client.Client;
import com.mobius.software.mqtt.performance.controller.net.NetworkHandler;
import com.mobius.software.mqtt.performance.controller.net.TCPClient;
import com.mobius.software.mqtt.performance.controller.task.TimedTask;

@Path("controller")
@Singleton
public class Controller
{
	private static final long TERMINATION_TIMEOUT = 1000;

	private List<Worker> workers = new ArrayList<>();
	private ExecutorService workersExecutor;
	private ScheduledExecutorService timersExecutor;
	private PeriodicQueuedTasks<TimedTask> scheduler;
	private LinkedBlockingQueue<TimedTask> mainQueue = new LinkedBlockingQueue<>();
	private IdentifierStorage identifierStorage = new IdentifierStorage();
	private ConcurrentHashMap<UUID, Orchestrator> scenarioMap = new ConcurrentHashMap<>();
	private NetworkHandler listener = new TCPClient();
	private Config config;

	public Controller() throws IOException
	{
		String path = "";
		try
		{
			path = Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
		}
		catch (Exception ex)
		{

		}

		File file = new File(path).getParentFile();
		file = new File(file.getPath() + "/" + ControllerRunner.configFile);
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		String value = new String(encoded, "UTF-8");
		String[] args = value.split("\\r?\\n");
		this.config = Config.parse(args);

		scheduler = new PeriodicQueuedTasks<TimedTask>(config.getTimersInterval(), mainQueue);
		workersExecutor = Executors.newFixedThreadPool(config.getWorkers());
		for (int i = 0; i < config.getWorkers(); i++)
		{
			Worker worker = new Worker(scheduler, mainQueue);
			workers.add(worker);
			workersExecutor.submit(worker);
		}
		timersExecutor = Executors.newScheduledThreadPool(2);
		timersExecutor.scheduleAtFixedRate(new PeriodicTasksRunner(scheduler), 0, config.getTimersInterval(), TimeUnit.MILLISECONDS);
	}

	@POST
	@Path("scenario")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenericJsonRequest scenario(ScenarioRequest json)
	{
		if (!json.validate())
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.INVALID_PARAMETERS);

		try
		{
			for (Scenario scenario : json.getRequests())
			{
				List<Client> clientList = new ArrayList<>();
				OrchestratorProperties properties = OrchestratorProperties.fromScenarioProperties(scenario.getId(), scenario.getProperties(), scenario.getThreshold(), scenario.getStartThreshold(), scenario.getContinueOnError(), config.getInitialDelay());
				Orchestrator orchestrator = new Orchestrator(properties, scheduler, clientList);
				String username = CommandParser.retrieveUsername(scenario.getCommands());
				listener.init(orchestrator.getProperties().getServerAddress());
				for (int i = 0; i < scenario.getCount(); i++)
				{
					String clientID = null;
					if (username != null)
					{
						int identityCounter = identifierStorage.countIdentity(properties.getIdentifierRegex(), properties.getStartIdentifier());
						clientID = IdentifierParser.parseIdentifier(properties.getIdentifierRegex(), username, properties.getServerHostname(), identityCounter);
					}
					Client client = new Client(clientID, orchestrator, listener, scenario.getCommands());
					clientList.add(client);
				}
				orchestrator.start();
				scenarioMap.put(scenario.getId(), orchestrator);
			}
			return new GenericJsonRequest(ResponseData.SUCCESS, null);
		}
		catch (Exception e)
		{
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.INTERNAL_SERVER_ERROR + e.getMessage());
		}
	}

	@POST
	@Path("report")
	@Produces(MediaType.APPLICATION_JSON)
	public ReportResponse report(UniqueIdentifierRequest json)
	{
		Orchestrator orchestrator = scenarioMap.get(json.getId());
		if (orchestrator == null)
			return new ReportResponse(ResponseData.NOT_FOUND);
		return orchestrator.report();
	}

	@POST
	@Path("clear")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenericJsonRequest clear(UniqueIdentifierRequest json)
	{
		if (!json.validate())
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.INVALID_PARAMETERS);

		Orchestrator orchestrator = scenarioMap.get(json.getId());
		if (orchestrator == null)
			return new GenericJsonRequest(ResponseData.ERROR, ResponseData.NOT_FOUND);

		orchestrator.terminate();

		return new GenericJsonRequest(ResponseData.SUCCESS, null);
	}

	public void shutdownGracefully()
	{
		shutdown(false);
	}

	public void shutdownNow()
	{
		shutdown(true);
	}

	private void shutdown(boolean mayInterrupt)
	{
		for (Worker worker : workers)
			worker.terminate();
		if (mayInterrupt)
			workersExecutor.shutdownNow();
		else
			workersExecutor.shutdown();

		if (mayInterrupt)
			timersExecutor.shutdownNow();
		else
			timersExecutor.shutdown();

		try
		{
			workersExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
			timersExecutor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e)
		{

		}
	}
}
