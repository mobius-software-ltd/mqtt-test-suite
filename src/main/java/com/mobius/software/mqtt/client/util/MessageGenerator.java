package com.mobius.software.mqtt.client.util;

import java.util.Random;

public class MessageGenerator
{
	
	private static final int MESSAGE_MIN_SIZE = 10;
	private static final int MESSAGE_MAX_SIZE = 90;

	private static final Random random = new Random();
	private static final String CHARACTERS = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ1234567890";

	public static byte[] generateContent()
	{
		int size = random.nextInt(MESSAGE_MAX_SIZE) + MESSAGE_MIN_SIZE;
		char[] text = new char[size];
		for (int i = 0; i < size; i++)
			text[i] = CHARACTERS.charAt(random.nextInt(CHARACTERS.length()));
		return new String(text).getBytes();
	}
}
