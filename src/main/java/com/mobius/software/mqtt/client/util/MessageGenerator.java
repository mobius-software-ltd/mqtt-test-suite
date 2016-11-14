package com.mobius.software.mqtt.client.util;

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
