package com.mobius.software.mqtt.performance.test;

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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.mobius.software.mqtt.performance.commons.util.IdentifierParser;

public class PerformanceTests
{
	@Test
	public void testIdentifierParser()
	{
		String regex = "%server%identity%";
		String username = "first@foo.bar";
		String server = "/192.168.0.100:1883";
		Integer startIdentifier = 1;
		int count = 10000;
		long start = System.currentTimeMillis();
		Set<String> set = new HashSet<>();
		for (int i = 0; i < count; i++)
			set.add(IdentifierParser.parseIdentifier(regex, username, server, startIdentifier));
		long stop = System.currentTimeMillis();
		System.out.println("time:" + (stop - start));
	}
}
