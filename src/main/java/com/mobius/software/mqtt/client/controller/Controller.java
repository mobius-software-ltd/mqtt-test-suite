package com.mobius.software.mqtt.client.controller;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

import com.mobius.software.mqtt.client.PerformanceClient;
import com.mobius.software.mqtt.client.api.data.Scenario;
import com.mobius.software.mqtt.client.api.json.GenericJsonResponse;
import com.mobius.software.mqtt.client.api.json.ResponseData;
import com.mobius.software.mqtt.client.api.json.ScenarioRequest;
import com.mobius.software.mqtt.client.api.json.UniqueIdentifierRequest;
import com.mobius.software.mqtt.client.controller.task.Timer;
import com.mobius.software.mqtt.client.net.NetworkListener;
import com.mobius.software.mqtt.client.net.TCPClient;

@Path("controller")
@Singleton
public class Controller
{
	private ExecutorService workersExecutor;
	private ScheduledExecutorService timersExecutor;
	private PeriodicQueuedTasks<Timer> scheduler;
	private LinkedBlockingQueue<Timer> mainQueue = new LinkedBlockingQueue<>();
	private ConcurrentHashMap<UUID, ScenarioOrchestrator> scenarioMap = new ConcurrentHashMap<>();
	private NetworkListener listener = new TCPClient();
	private Config config;

	public Controller() throws IOException
	{
		//		ClassLoader classLoader = getClass().getClassLoader();
		//		File file = new File(classLoader.getResource("controller.params").getFile());
		File file = new File(ControllerRunner.configFile);
		byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
		String value = new String(encoded, "UTF-8");
		String[] args = value.split("\\r?\\n");
		this.config = Config.parse(args);

		scheduler = new PeriodicQueuedTasks<Timer>(config.getTimersInterval(), mainQueue);
		workersExecutor = Executors.newFixedThreadPool(config.getWorkers());
		for (int i = 0; i < config.getWorkers(); i++)
			workersExecutor.submit(new ScenarioRunner(scheduler, mainQueue));

		timersExecutor = Executors.newScheduledThreadPool(2);
		timersExecutor.scheduleAtFixedRate(new TimersRunner(scheduler), 0, config.getTimersInterval(), TimeUnit.MILLISECONDS);
	}

	@POST
	@Path("scenario")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenericJsonResponse scenario(ScenarioRequest json)
	{
		if (!json.validate())
			return new GenericJsonResponse(ResponseData.ERROR, ResponseData.INVALID_PARAMETERS);

		try
		{
			for (Scenario scenario : json.getRequests())
			{
				List<PerformanceClient> clientList = new ArrayList<>();
				ScenarioOrchestrator orchestrator = new ScenarioOrchestrator(scheduler, clientList, scenario.getThreshold(), config.getInitialDelay());
				SocketAddress serverAddress = new InetSocketAddress(scenario.getProperties().getServerHostname(), scenario.getProperties().getServerPort());
				for (int i = 0; i < scenario.getCount(); i++)
				{
					PerformanceClient client = new PerformanceClient(scheduler, listener, scenario.getCommands(), serverAddress, scenario.getProperties().getResendInterval(), scenario.getContinueOnError(), orchestrator, config.getInitialDelay());
					clientList.add(client);
				}

				orchestrator.start();
				scenarioMap.put(scenario.getId(), orchestrator);
			}

			return new GenericJsonResponse(ResponseData.SUCCESS, null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return new GenericJsonResponse(ResponseData.ERROR, ResponseData.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Path("report")
	@Produces(MediaType.APPLICATION_JSON)
	public Report report(UniqueIdentifierRequest json)
	{
		ScenarioOrchestrator orchestrator = scenarioMap.get(json.getId());
		if (orchestrator == null)
			return new Report();
		return orchestrator.report();
	}

	@POST
	@Path("clear")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public GenericJsonResponse clear(UniqueIdentifierRequest json)
	{
		if (!json.validate())
			return new GenericJsonResponse(ResponseData.ERROR, ResponseData.INVALID_PARAMETERS);

		ScenarioOrchestrator orchestrator = scenarioMap.get(json.getId());
		if (orchestrator == null)
			return new GenericJsonResponse(ResponseData.ERROR, ResponseData.NOT_FOUND);

		orchestrator.terminate();

		return new GenericJsonResponse(ResponseData.SUCCESS, null);
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
		if (mayInterrupt)
			workersExecutor.shutdownNow();
		else
			workersExecutor.shutdown();

		if (mayInterrupt)
			timersExecutor.shutdownNow();
		else
			timersExecutor.shutdown();
	}
}
