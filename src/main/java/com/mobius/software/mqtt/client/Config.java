package com.mobius.software.mqtt.client;

import java.net.URI;
import java.util.Arrays;

public class Config
{
	private static final String NEW_LINE = System.getProperty("line.separator");

	private static final String BASE_URL = "-baseURL=";
	private static final String SEREVER_HOSTNAME = "-host=";
	private static final String SERVER_PORT = "-port=";
	private static final String REGEX_TEMPLATE = "-identRegex=";
	private static final String USERNAME = "-username=";
	private static final String PASSWORD = "-password=";
	private static final String CLEAN = "-clean=";
	private static final String KEEPALIVE = "-keepalive=";
	private static final String RESEND_INTERVAL = "-resendInterval=";
	private static final String SUBSCRIBERS = "-subscribers=";
	private static final String PUBLISHERS = "-publishers=";
	private static final String THRESHOLD = "-threshold=";
	private static final String TOPIC = "-topic=";
	private static final String QOS = "-qos=";
	private static final String MESSAGE_COUNT = "-messageCount=";
	private static final String CONNECT_DELAY = "-connectDelay=";
	private static final String SUBSCRIBE_DELAY = "-subscribeDelay=";
	private static final String PUBLISH_DELAY = "-publishDelay=";
	private static final String UNSUBSCRIBE_DELAY = "-unsubscribeDelay=";
	private static final String DISCONNECT_DELAY = "-disconnectDelay=";

	private static final String ERROR_MESSAGE = "An eror occured while parsing argument: ";

	private URI baseURL;
	private String serverHostname;
	private Integer serverPort;
	private String identRegex;
	private String username;
	private String password;
	private Boolean clean;
	private Integer keepalive;
	private Long resendInterval;
	private Integer subscribers;
	private Integer publishers;
	private Integer threshold;
	private String topic;
	private Integer qos;
	private Integer messageCount;
	private Long connectDelay;
	private Long subscribeDelay;
	private Long publishDelay;
	private Long unsubscribeDelay;
	private Long disconnectDelay;

	private Config(URI baseURL, String serverHostname, Integer serverPort, String identRegex, String username, String password, Boolean clean, Integer keepalive, Long resendInterval, Integer subscribers, Integer publishers, Integer threshold, String topic, Integer qos, Integer messageCount, Long connectDelay, Long subscribeDelay, Long publishDelay, Long unsubscribeDelay, Long disconnectDelay)
	{
		this.baseURL = baseURL;
		this.serverHostname = serverHostname;
		this.serverPort = serverPort;
		this.identRegex = identRegex;
		this.username = username;
		this.password = password;
		this.clean = clean;
		this.keepalive = keepalive;
		this.resendInterval = resendInterval;
		this.subscribers = subscribers;
		this.publishers = publishers;
		this.threshold = threshold;
		this.topic = topic;
		this.qos = qos;
		this.messageCount = messageCount;
		this.connectDelay = connectDelay;
		this.subscribeDelay = subscribeDelay;
		this.publishDelay = publishDelay;
		this.unsubscribeDelay = unsubscribeDelay;
		this.disconnectDelay = disconnectDelay;
	}

	public static Config parse(String[] args)
	{
		URI baseURL = null;
		String serverHostname = null, identRegex = null, username = null, password = null, topic = null;
		Integer serverPort = null, keepalive = null, subscribers = null, publishers = null, qos = null, threshold = null, messageCount = null;
		Long resendInterval = null, connectDelay = null, subscribeDelay = null, publishDelay = null, unsubscribeDelay = null, disconnectDelay = null;
		Boolean clean = null;
		try
		{
			if (!args[0].startsWith(BASE_URL))
				throw new IllegalArgumentException(ERROR_MESSAGE + BASE_URL + ", args: " + Arrays.asList(args));
			baseURL = URI.create(args[0].replace(BASE_URL, ""));

			if (!args[1].startsWith(SEREVER_HOSTNAME))
				throw new IllegalArgumentException(ERROR_MESSAGE + SEREVER_HOSTNAME + ", args: " + Arrays.asList(args));
			serverHostname = args[1].replace(SEREVER_HOSTNAME, "");

			if (!args[2].startsWith(SERVER_PORT))
				throw new IllegalArgumentException(ERROR_MESSAGE + SERVER_PORT + ", args: " + Arrays.asList(args));
			serverPort = Integer.parseInt(args[2].replace(SERVER_PORT, ""));

			if (!args[3].startsWith(REGEX_TEMPLATE))
				throw new IllegalArgumentException(ERROR_MESSAGE + REGEX_TEMPLATE + ", args: " + Arrays.asList(args));
			identRegex = args[3].replace(REGEX_TEMPLATE, "");

			if (!args[4].startsWith(USERNAME))
				throw new IllegalArgumentException(ERROR_MESSAGE + USERNAME + ", args: " + Arrays.asList(args));
			username = args[4].replace(USERNAME, "");

			if (!args[5].startsWith(PASSWORD))
				throw new IllegalArgumentException(ERROR_MESSAGE + PASSWORD + ", args: " + Arrays.asList(args));
			password = args[5].replace(PASSWORD, "");

			if (!args[6].startsWith(CLEAN))
				throw new IllegalArgumentException(ERROR_MESSAGE + CLEAN + ", args: " + Arrays.asList(args));
			clean = Boolean.parseBoolean(args[6].replace(CLEAN, ""));

			if (!args[7].startsWith(KEEPALIVE))
				throw new IllegalArgumentException(ERROR_MESSAGE + KEEPALIVE + ", args: " + Arrays.asList(args));
			keepalive = Integer.parseInt(args[7].replace(KEEPALIVE, ""));

			if (!args[8].startsWith(RESEND_INTERVAL))
				throw new IllegalArgumentException(ERROR_MESSAGE + RESEND_INTERVAL + ", args: " + Arrays.asList(args));
			resendInterval = Long.parseLong(args[8].replace(RESEND_INTERVAL, ""));

			if (!args[9].startsWith(SUBSCRIBERS))
				throw new IllegalArgumentException(ERROR_MESSAGE + SUBSCRIBERS + ", args: " + Arrays.asList(args));
			subscribers = Integer.parseInt(args[9].replace(SUBSCRIBERS, ""));

			if (!args[10].startsWith(PUBLISHERS))
				throw new IllegalArgumentException(ERROR_MESSAGE + PUBLISHERS + ", args: " + Arrays.asList(args));
			publishers = Integer.parseInt(args[10].replace(PUBLISHERS, ""));

			if (!args[11].startsWith(THRESHOLD))
				throw new IllegalArgumentException(ERROR_MESSAGE + THRESHOLD + ", args: " + Arrays.asList(args));
			threshold = Integer.parseInt(args[11].replace(THRESHOLD, ""));

			if (!args[12].startsWith(TOPIC))
				throw new IllegalArgumentException(ERROR_MESSAGE + TOPIC + ", args: " + Arrays.asList(args));
			topic = args[12].replace(TOPIC, "");

			if (!args[13].startsWith(QOS))
				throw new IllegalArgumentException(ERROR_MESSAGE + QOS + ", args: " + Arrays.asList(args));
			qos = Integer.parseInt(args[13].replace(QOS, ""));

			if (!args[14].startsWith(MESSAGE_COUNT))
				throw new IllegalArgumentException(ERROR_MESSAGE + MESSAGE_COUNT + ", args: " + Arrays.asList(args));
			messageCount = Integer.parseInt(args[14].replace(MESSAGE_COUNT, ""));

			if (!args[15].startsWith(CONNECT_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + CONNECT_DELAY + ", args: " + Arrays.asList(args));
			connectDelay = Long.parseLong(args[15].replace(CONNECT_DELAY, ""));

			if (!args[16].startsWith(SUBSCRIBE_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + SUBSCRIBE_DELAY + ", args: " + Arrays.asList(args));
			if (args[16].replace(SUBSCRIBE_DELAY, "").length() > 0)
				subscribeDelay = Long.parseLong(args[16].replace(SUBSCRIBE_DELAY, ""));

			if (!args[17].startsWith(PUBLISH_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + PUBLISH_DELAY + ", args: " + Arrays.asList(args));
			if (args[17].replace(PUBLISH_DELAY, "").length() > 0)
				publishDelay = Long.parseLong(args[17].replace(PUBLISH_DELAY, ""));

			if (!args[18].startsWith(UNSUBSCRIBE_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + UNSUBSCRIBE_DELAY + ", args: " + Arrays.asList(args));
			if (args[18].replace(UNSUBSCRIBE_DELAY, "").length() > 0)
				unsubscribeDelay = Long.parseLong(args[18].replace(UNSUBSCRIBE_DELAY, ""));

			if (!args[19].startsWith(DISCONNECT_DELAY))
				throw new IllegalArgumentException(ERROR_MESSAGE + DISCONNECT_DELAY + ", args: " + Arrays.asList(args));
			disconnectDelay = Long.parseLong(args[19].replace(DISCONNECT_DELAY, ""));
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(e.getMessage());
		}

		return new Config(baseURL, serverHostname, serverPort, identRegex, username, password, clean, keepalive, resendInterval, subscribers, publishers, threshold, topic, qos, messageCount, connectDelay, subscribeDelay, publishDelay, unsubscribeDelay, disconnectDelay);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(BASE_URL).append(baseURL).append(NEW_LINE);
		sb.append(SEREVER_HOSTNAME).append(serverHostname).append(NEW_LINE);
		sb.append(SERVER_PORT).append(serverPort).append(NEW_LINE);
		sb.append(REGEX_TEMPLATE).append(identRegex).append(NEW_LINE);
		sb.append(USERNAME).append(username).append(NEW_LINE);
		sb.append(PASSWORD).append(password).append(NEW_LINE);
		sb.append(CLEAN).append(clean).append(NEW_LINE);
		sb.append(KEEPALIVE).append(keepalive).append(NEW_LINE);
		sb.append(RESEND_INTERVAL).append(resendInterval).append(NEW_LINE);
		sb.append(SUBSCRIBERS).append(subscribers).append(NEW_LINE);
		sb.append(PUBLISHERS).append(publishers).append(NEW_LINE);
		sb.append(THRESHOLD).append(threshold).append(NEW_LINE);
		sb.append(TOPIC).append(topic).append(NEW_LINE);
		sb.append(QOS).append(qos).append(NEW_LINE);
		sb.append(MESSAGE_COUNT).append(messageCount).append(NEW_LINE);
		sb.append(CONNECT_DELAY).append(connectDelay).append(NEW_LINE);
		sb.append(SUBSCRIBE_DELAY).append(subscribeDelay).append(NEW_LINE);
		sb.append(PUBLISH_DELAY).append(publishDelay).append(NEW_LINE);
		sb.append(UNSUBSCRIBE_DELAY).append(unsubscribeDelay).append(NEW_LINE);
		sb.append(DISCONNECT_DELAY).append(disconnectDelay).append(NEW_LINE);
		return sb.toString();
	}

	public URI getBaseURL()
	{
		return baseURL;
	}

	public String getServerHostname()
	{
		return serverHostname;
	}

	public Integer getServerPort()
	{
		return serverPort;
	}

	public String getIdentRegex()
	{
		return identRegex;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public Boolean getClean()
	{
		return clean;
	}

	public Integer getKeepalive()
	{
		return keepalive;
	}

	public Long getResendInterval()
	{
		return resendInterval;
	}

	public Integer getSubscribers()
	{
		return subscribers;
	}

	public Integer getPublishers()
	{
		return publishers;
	}

	public Integer getThreshold()
	{
		return threshold;
	}

	public String getTopic()
	{
		return topic;
	}

	public Integer getQos()
	{
		return qos;
	}

	public Integer getMessageCount()
	{
		return messageCount;
	}

	public Long getConnectDelay()
	{
		return connectDelay;
	}

	public Long getSubscribeDelay()
	{
		return subscribeDelay;
	}

	public Long getPublishDelay()
	{
		return publishDelay;
	}

	public Long getUnsubscribeDelay()
	{
		return unsubscribeDelay;
	}

	public Long getDisconnectDelay()
	{
		return disconnectDelay;
	}
}